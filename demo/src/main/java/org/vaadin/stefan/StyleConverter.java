/*
 * Copyright 2018, Stefan Uebe
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
package org.vaadin.stefan;

import com.steadystate.css.dom.CSSStyleDeclarationImpl;
import com.steadystate.css.dom.CSSStyleRuleImpl;
import com.steadystate.css.dom.Property;
import com.steadystate.css.parser.CSSOMParser;
import com.steadystate.css.parser.SACParserCSS3;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SelectorList;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleSheet;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class StyleConverter {
    public static void main(String[] args) throws Exception {
        Path styles = Paths.get("styles").toAbsolutePath();

        Files.list(styles).filter(path -> path.toString().endsWith(".css")).forEach(path -> {
            try {

                CSSOMParser parser = new CSSOMParser(new SACParserCSS3());
                CSSStyleSheet styleSheet = parser.parseStyleSheet(new InputSource(path.toUri().toString()), null, null);

                Map<String, String> defs = new HashMap<>();
                Set<String> usage = new HashSet<>();

                CSSRuleList rules = styleSheet.getCssRules();
                for (int i = 0; i < rules.getLength(); i++) {
                    final CSSRule rule = rules.item(i);

                    if (rule instanceof CSSStyleRuleImpl) {
                        CSSStyleRuleImpl sRule = (CSSStyleRuleImpl) rule;

                        SelectorList selectors = sRule.getSelectors();
                        List<Property> properties = ((CSSStyleDeclarationImpl) sRule.getStyle()).getProperties();

                        for (int j = 0; j < selectors.getLength(); j++) {
                            Selector item = selectors.item(j);
                            String prefix = item.toString().trim();
                            prefix = prefix.replace(".", "");
                            prefix = prefix.replace(" ", "_");
                            prefix = prefix.replace(">", "_LACE_BRACE_");
                            prefix = prefix.replace(":", "_COLON_");
                            prefix = prefix.replace("*", "_ASTERISK_");
                            prefix = prefix.replace("+", "_PLUS_");
                            prefix = prefix.replace("[", "_SQUARE_BRACKET_OPEN_");
                            prefix = prefix.replace("]", "_SQUARE_BRACKET_CLOSE_");
                            prefix = prefix.replace("(", "_R_BRACKET_OPEN_");
                            prefix = prefix.replace(")", "_R_BRACKET_CLOSE_");


                            for (Property property : properties) {
                                String fullName = prefix + "-" + property.getName().trim();

                                String cssText = property.getValue().getCssText();
                                if (cssText.matches("-[a-zA-Z].*")) {
                                    fullName += "-" + cssText.split("\\(")[0].trim();
                                }


//                        if (cssText.startsWith("rgb") ) {
//                            fullName += "-" + cssText.split("\\(")[0].trim();
//                        }

                                if (defs.containsKey(fullName) && !defs.get(fullName).equals(cssText)) {
                                    System.err.println(fullName + " already in there with diff css text: " + cssText + " vs. " + defs.get(fullName));
                                } else {
                                    defs.put(fullName, cssText);
                                    usage.add(item.toString() + " { " + property.getName() + ": var(--" + fullName + ", " + cssText + "); }");
                                }
                            }
                        }
                    }

                    BufferedWriter bufferedWriter = Files.newBufferedWriter(Paths.get(path.toString() + ".def"));

                    bufferedWriter.write("<custom-style>\n    <style>\n        html{\n");

                    defs.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey)).forEach(e -> {
                        try {
                            bufferedWriter.write("            --" + e.getKey() + ": " + e.getValue() + ";");
                            bufferedWriter.newLine();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    });
                    bufferedWriter.write("        }\n    </style>\n</custom-style>");
                    bufferedWriter.close();

                    BufferedWriter bufferedWriter2 = Files.newBufferedWriter(Paths.get(path.toString() + ".usage"));
                    usage.stream().sorted(Comparator.naturalOrder()).forEach(s -> {
                        try {
                            bufferedWriter2.write(s);
//                    bufferedWriter2.newLine();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });

                    bufferedWriter2.close();
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

}
