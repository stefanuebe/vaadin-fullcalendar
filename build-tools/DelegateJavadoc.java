/*
 * Copyright 2026, Stefan Uebe
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Copies Javadoc onto the methods that Lombok's {@code @Delegate} generates, in delomboked sources.
 * <p>
 * Delombok writes a delegating method without Javadoc, for example {@code FullCalendarOptions.setLocale}, which only
 * calls {@code this.calendarOptions().setLocale(value)}. This tool finds the called method in the delegate's class,
 * {@code CalendarOptions} in that example, and puts its Javadoc above the delegating method. It also adds the imports
 * of that class, so the links in the copied Javadoc resolve. The sources jar then shows the Javadoc in an IDE without
 * Lombok (ticket #481).
 * <p>
 * Run with the delombok output folder as the only argument: {@code java DelegateJavadoc.java target/delombok}. It
 * edits the files in place and fails when a delegating method finds no Javadoc to copy.
 */
public class DelegateJavadoc {

    /**
     * A delegating method as delombok writes it: one line that calls the same method on an accessor. A delegating
     * method in another shape is not found and keeps no Javadoc.
     */
    private static final Pattern DELEGATING_METHOD = Pattern.compile(
            "(?m)^ {4}public [^\\n{]*? (?<name>\\w+)\\((?<parameters>[^)]*)\\) \\{\\n"
                    + "\\s+(?:return )?this\\.(?<accessor>\\w+)\\(\\)\\.\\k<name>\\([^)]*\\);\\n\\s+}");

    /** A Javadoc comment, optional annotations and the signature of the public method it documents. */
    private static final Pattern DOCUMENTED_METHOD = Pattern.compile(
            "(?m)^(?<javadoc> {4}/\\*\\*(?:(?!\\*/)[\\s\\S])*\\*/)\\n(?:\\s*@[\\w.]+(?:\\([^)]*\\))?\\n)*"
                    + "\\s*public [^\\n(=]*? (?<name>\\w+)\\((?<parameters>[^)]*)\\)");

    private static final Pattern IMPORT = Pattern.compile("(?m)^import [\\w.]+(?:\\.\\*)?;$");

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: java DelegateJavadoc.java <delombok output folder>");
        }
        Path root = Path.of(args[0]);
        Map<String, Path> filesByType = new HashMap<>();
        try (Stream<Path> files = Files.walk(root)) {
            files.filter(file -> file.toString().endsWith(".java"))
                    .forEach(file -> {
                        Path other = filesByType.put(file.getFileName().toString().replace(".java", ""), file);
                        if (other != null) {
                            throw new IllegalStateException("Two classes share a name: " + other + ", " + file);
                        }
                    });
        }

        List<String> missing = new ArrayList<>();
        int copied = 0;
        for (Path file : filesByType.values()) {
            String source = Files.readString(file);
            Matcher delegatingMethods = DELEGATING_METHOD.matcher(source);
            StringBuilder result = new StringBuilder();
            Set<String> delegateImports = new LinkedHashSet<>();
            int last = 0;
            while (delegatingMethods.find()) {
                String name = delegatingMethods.group("name");
                String parameters = delegatingMethods.group("parameters");
                // A second run finds the Javadoc of the first one.
                if (source.substring(0, delegatingMethods.start()).stripTrailing().endsWith("*/")) {
                    continue;
                }
                String delegateType = accessorType(source, delegatingMethods.group("accessor"));
                Path delegateFile = delegateType == null ? null : filesByType.get(delegateType);
                String javadoc = delegateFile == null ? null
                        : javadocsBySignature(Files.readString(delegateFile)).get(signature(name, parameters));
                if (javadoc == null) {
                    missing.add(file.getFileName() + ": " + name + "(" + parameters + ")");
                    continue;
                }
                delegateImports.addAll(imports(Files.readString(delegateFile)));
                result.append(source, last, delegatingMethods.start()).append(javadoc).append('\n');
                last = delegatingMethods.start();
                copied++;
            }
            if (last > 0) {
                result.append(source.substring(last));
                Files.writeString(file, addImports(result.toString(), delegateImports));
            }
        }

        if (!missing.isEmpty()) {
            missing.sort(null);
            throw new IllegalStateException("No Javadoc found for these delegating methods:\n" + String.join("\n", missing));
        }
        System.out.println("DelegateJavadoc: copied the Javadoc of " + copied + " delegating methods");
    }

    /** The return type of the accessor the delegating method calls, such as {@code CalendarOptions}. */
    private static String accessorType(String source, String accessor) {
        Matcher matcher = Pattern.compile("(\\w+) " + accessor + "\\(\\) \\{").matcher(source);
        return matcher.find() ? matcher.group(1) : null;
    }

    /** The Javadoc of each documented public method, by its signature. */
    private static Map<String, String> javadocsBySignature(String source) {
        Map<String, String> javadocs = new HashMap<>();
        Matcher matcher = DOCUMENTED_METHOD.matcher(source);
        while (matcher.find()) {
            javadocs.putIfAbsent(signature(matcher.group("name"), matcher.group("parameters")),
                    matcher.group("javadoc"));
        }
        return javadocs;
    }

    /**
     * Name and parameter types without annotations, {@code final}, package names and parameter names. The delegating
     * method writes its types with packages, the delegate's own method often without.
     */
    private static String signature(String name, String parameters) {
        List<String> types = new ArrayList<>();
        int depth = 0;
        StringBuilder parameter = new StringBuilder();
        for (char c : (parameters + ",").toCharArray()) {
            if (c == ',' && depth == 0) {
                String type = parameter.toString()
                        .replaceAll("@[\\w.]+", "")
                        .replaceAll("\\bfinal\\b", "")
                        .replaceAll("\\b(?:[a-z_]\\w*\\.)+(?=[A-Z])", "")
                        .trim();
                if (!type.isEmpty()) {
                    types.add(type.substring(0, type.lastIndexOf(' ')).replaceAll("\\s+", ""));
                }
                parameter.setLength(0);
                continue;
            }
            depth += c == '<' ? 1 : c == '>' ? -1 : 0;
            parameter.append(c);
        }
        return name + types;
    }

    private static Set<String> imports(String source) {
        Set<String> imports = new LinkedHashSet<>();
        IMPORT.matcher(source).results().forEach(match -> imports.add(match.group()));
        return imports;
    }

    /** Adds the imports the file does not have yet, after its package declaration. */
    private static String addImports(String source, Set<String> imports) {
        imports.removeAll(imports(source));
        if (imports.isEmpty()) {
            return source;
        }
        int packageStart = source.indexOf("\npackage ");
        if (packageStart < 0) {
            throw new IllegalStateException("No package declaration to put the imports after");
        }
        int packageEnd = source.indexOf(";", packageStart) + 1;
        return source.substring(0, packageEnd) + "\n\n" + String.join("\n", imports) + source.substring(packageEnd);
    }
}
