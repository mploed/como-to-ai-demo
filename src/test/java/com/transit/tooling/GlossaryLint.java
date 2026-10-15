package com.transit.tooling;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/** Fails when code of a context uses words the modeling session decided to avoid. */
public final class GlossaryLint {

    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    private GlossaryLint() {
    }

    public static List<String> check(Glossary glossary, Path sourceRoot, Path reportRelativeTo) {
        Path packageDir = sourceRoot.resolve(glossary.packageName().replace('.', '/'));
        List<String> violations = new ArrayList<>();
        try (Stream<Path> files = Files.walk(packageDir)) {
            for (Path file : files.filter(f -> f.toString().endsWith(".java")).sorted().toList()) {
                List<String> lines = Files.readAllLines(file);
                for (int i = 0; i < lines.size(); i++) {
                    String line = lines.get(i).trim();
                    if (line.startsWith("//") || line.startsWith("*") || line.startsWith("/*")) {
                        continue;
                    }
                    for (String word : wordsIn(line)) {
                        for (Glossary.Term term : glossary.terms()) {
                            for (Glossary.Avoided avoided : term.avoid()) {
                                if (avoided.word().toLowerCase(Locale.ROOT).equals(word)) {
                                    violations.add(reportRelativeTo.relativize(file) + ":" + (i + 1) + "\n"
                                            + "  \"" + avoided.word() + "\" is an avoided term in " + glossary.context()
                                            + ". Use: " + avoided.use() + ".\n"
                                            + "  Rejected because: " + avoided.reason() + ". (" + avoided.decision() + ")");
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot scan " + packageDir, e);
        }
        return violations.stream().distinct().toList();
    }

    /** Splits identifiers like BookingRefundService into booking, refund, service. */
    static List<String> wordsIn(String line) {
        List<String> words = new ArrayList<>();
        Matcher m = IDENTIFIER.matcher(line);
        while (m.find()) {
            for (String part : m.group().split("_|(?<=[a-z0-9])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])")) {
                if (!part.isEmpty()) {
                    words.add(part.toLowerCase(Locale.ROOT));
                }
            }
        }
        return words;
    }
}
