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
 * Completes the Javadoc of the delomboked sources, so the sources jar shows it in an IDE without Lombok.
 * <p>
 * First, it copies Javadoc onto the methods that Lombok's {@code @Delegate} generates. Delombok writes a delegating
 * method without Javadoc, for example {@code FullCalendarOptions.setLocale}, which only calls
 * {@code this.calendarOptions().setLocale(value)}. This tool finds the called method in the delegate's class,
 * {@code CalendarOptions} in that example, and puts its Javadoc above the delegating method. It also adds the imports
 * of that class, so the links in the copied Javadoc resolve (ticket #481).
 * <p>
 * Then it completes the hand-written option setters. They only say what they set and refer to the getter, such as
 * {@code Sets whether dragged or resized entries may overlap entries, see {@link #getEntryOverlap()}.} The repository
 * keeps one text per option, on the field, which Lombok copies to the getter. A setter marks where the option's
 * description goes with the placeholder {@link #PLACEHOLDER}. This tool puts the getter's description there and drops
 * the reference to the getter, so the setter explains the option itself (ticket #483).
 * <p>
 * Run with the delombok output folder as the only argument: {@code java SourcesJavadoc.java target/delombok}. It
 * edits the files in place and fails when a delegating method or a setter finds no Javadoc to copy, or when a
 * placeholder stays.
 */
public class SourcesJavadoc {

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

    /** The line in a setter's Javadoc that the getter's description replaces. */
    private static final String PLACEHOLDER =
            "<!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->";

    /**
     * The reference to a getter, without the final period: {@code , see {@link #getX()}},
     * {@code . See {@link #getX()}} or {@code  (see {@link #getX()})}. {@code %s} stands for the getter's name.
     */
    private static final String GETTER_REFERENCE = "(?:,|\\.|\\s*\\()\\s*[Ss]ee\\s+\\{@link #%s\\(\\)}\\)?(?=\\.)";

    private static final Pattern IMPORT = Pattern.compile("(?m)^import [\\w.]+(?:\\.\\*)?;$");

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: java SourcesJavadoc.java <delombok output folder>");
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

        int copied = copyDelegateJavadoc(filesByType);
        int completed = completeSetterJavadoc(filesByType);
        System.out.println("SourcesJavadoc: copied the Javadoc of " + copied + " delegating methods, completed the"
                + " Javadoc of " + completed + " setters");
    }

    /**
     * Copies the Javadoc of the called method onto each delegating method and returns how many it copied. Runs before
     * {@link #completeSetterJavadoc}, so a hand-written setter of a delegating class finds the Javadoc of the
     * delegated getter, and the copied setters get completed as well.
     */
    private static int copyDelegateJavadoc(Map<String, Path> filesByType) throws IOException {
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

        failOnMissing("No Javadoc found for these delegating methods", missing);

        return copied;
    }

    /**
     * Replaces the placeholder of each setter by the description of its getter, drops the setter's reference to the
     * getter and returns how many setters it completed.
     */
    private static int completeSetterJavadoc(Map<String, Path> filesByType) throws IOException {
        List<String> missing = new ArrayList<>();
        int completed = 0;
        for (Path file : filesByType.values()) {
            String source = Files.readString(file);
            if (!source.contains(PLACEHOLDER)) {
                continue;
            }

            StringBuilder result = new StringBuilder();
            int last = 0;
            // A placeholder that no setter replaces fails the build, unless the setter is already in missing.
            int placeholdersLeft = 0;
            Map<String, String> javadocs = javadocsBySignature(source);
            Matcher methods = DOCUMENTED_METHOD.matcher(source);
            while (methods.find()) {
                String name = methods.group("name");
                List<String> setter = javadocLines(methods.group("javadoc"));
                if (!name.startsWith("set") || !setter.contains(PLACEHOLDER)) {
                    continue;
                }

                String getterName = "get" + name.substring(3);
                String getter = javadocs.get(signature(getterName, ""));
                List<String> getterLines = getter == null ? List.of() : javadocLines(getter);
                String setterText = String.join("\n", setter.subList(0, setter.indexOf(PLACEHOLDER)));
                String withoutReference = setterText.replaceFirst(String.format(GETTER_REFERENCE, getterName), "");
                if (description(getterLines).isEmpty() || withoutReference.contains("#" + getterName + "()")) {
                    missing.add(file.getFileName() + ": " + name + "(" + methods.group("parameters") + ")");
                    placeholdersLeft--;
                    continue;
                }

                result.append(source, last, methods.start("javadoc"))
                        .append(withGetterDescription(setter, withoutReference, getterLines));
                last = methods.end("javadoc");
                completed++;
            }
            result.append(source.substring(last));
            placeholdersLeft += result.toString().split(Pattern.quote(PLACEHOLDER), -1).length - 1;
            if (placeholdersLeft > 0) {
                missing.add(file.getFileName() + ": a placeholder outside a setter with a getter");
            }
            Files.writeString(file, result);
        }
        failOnMissing("Setter Javadoc not completed. The getter has no Javadoc, the setter refers to it in another form"
                + " than \", see {@link #getX()}.\", or a placeholder is outside such a setter", missing);

        return completed;
    }

    private static void failOnMissing(String message, List<String> missing) {
        if (!missing.isEmpty()) {
            missing.sort(null);
            throw new IllegalStateException(message + ":\n" + String.join("\n", missing));
        }
    }

    /**
     * The setter's Javadoc with the getter's description in place of the placeholder, set apart from the setter's text
     * by a line and the heading "Option description", and with the getter's {@code @see} tags after the setter's
     * tags. {@code setterText} is the setter's text before the placeholder, without the reference to the getter.
     */
    private static String withGetterDescription(List<String> setter, String setterText, List<String> getter) {
        int placeholder = setter.indexOf(PLACEHOLDER);
        List<String> lines = new ArrayList<>(List.of(setterText.split("\n", -1)));
        stripTrailingBlankLines(lines);
        if (!lines.isEmpty()) {
            lines.add("<hr>");
            lines.add("<b>Option description</b>");
            lines.add("<p>");
        }
        lines.addAll(description(getter));
        lines.addAll(setter.subList(placeholder + 1, setter.size()));
        stripTrailingBlankLines(lines);
        boolean see = false;
        for (String line : getter.subList(tagStart(getter), getter.size())) {
            see = line.startsWith("@") ? line.startsWith("@see ") && !setter.contains(line) : see;
            if (see) {
                lines.add(line);
            }
        }
        StringBuilder javadoc = new StringBuilder("    /**\n");
        lines.forEach(line -> javadoc.append(line.isEmpty() ? "     *" : "     * " + line).append('\n'));

        return javadoc.append("     */").toString();
    }

    /** The lines before the first tag, without the blank lines at their end. */
    private static List<String> description(List<String> lines) {
        List<String> description = new ArrayList<>(lines.subList(0, tagStart(lines)));
        stripTrailingBlankLines(description);

        return description;
    }

    private static void stripTrailingBlankLines(List<String> lines) {
        while (!lines.isEmpty() && lines.get(lines.size() - 1).isBlank()) {
            lines.remove(lines.size() - 1);
        }
    }

    /** The lines of a Javadoc comment without the comment markers and the leading {@code * }. */
    private static List<String> javadocLines(String javadoc) {
        String[] lines = javadoc.split("\n");
        List<String> result = new ArrayList<>();
        for (int i = 1; i < lines.length - 1; i++) {
            result.add(lines[i].replaceFirst("^\\s*\\* ?", ""));
        }

        return result;
    }

    /** The index of the first tag line, or the number of lines when there is none. */
    private static int tagStart(List<String> lines) {
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith("@")) {
                return i;
            }
        }

        return lines.size();
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
