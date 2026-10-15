package com.transit.tooling;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Runs the glossaries against a request before anything gets built.
 * Simple token matching, no NLP: it finds words we decided against and words that mean
 * different things in different contexts, and turns them into questions back to the requester.
 */
public final class PromptLint {

    private PromptLint() {
    }

    public static List<String> questionsFor(String prompt, Path repoRoot) {
        List<Glossary> glossaries = loadAll(repoRoot.resolve("contexts"));
        Set<String> words = new LinkedHashSet<>();
        for (String w : GlossaryLint.wordsIn(prompt)) {
            words.add(w);
            if (w.endsWith("s") && w.length() > 3) {
                words.add(w.substring(0, w.length() - 1));
            }
        }
        List<String> questions = new ArrayList<>();
        for (Glossary glossary : glossaries) {
            for (Glossary.Term term : glossary.terms()) {
                if (words.contains(term.term().toLowerCase(Locale.ROOT)) && !term.notToBeConfusedWith().isEmpty()) {
                    StringBuilder q = new StringBuilder("'" + term.term() + "' in which context? ")
                            .append(term.term()).append(" (").append(glossary.context()).append(")");
                    term.notToBeConfusedWith().forEach(c -> q.append(", ").append(c.term()).append(" (").append(c.context()).append(")"));
                    questions.add(q.append(".").toString());
                }
                for (Glossary.Avoided avoided : term.avoid()) {
                    if (words.contains(avoided.word().toLowerCase(Locale.ROOT))) {
                        questions.add("'" + avoided.word() + "' is not a term in " + glossary.context()
                                + ". Do you mean " + avoided.use() + "? (" + avoided.reason() + ", " + avoided.decision() + ")");
                    }
                }
            }
        }
        return questions;
    }

    private static List<Glossary> loadAll(Path contexts) {
        try (Stream<Path> files = Files.walk(contexts, 2)) {
            return files.filter(f -> f.getFileName().toString().equals("glossary.yaml")).sorted().map(Glossary::load).toList();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read glossaries in " + contexts, e);
        }
    }

    public static void main(String[] args) {
        String prompt = String.join(" ", args);
        List<String> questions = questionsFor(prompt, Path.of("."));
        System.out.println("> " + prompt);
        if (questions.isEmpty()) {
            System.out.println("No questions from the glossaries.");
            return;
        }
        questions.forEach(q -> System.out.println("? " + q));
    }
}
