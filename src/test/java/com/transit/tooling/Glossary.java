package com.transit.tooling;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/** The Ubiquitous Language of one Bounded Context, as harvested into glossary.yaml. */
public record Glossary(String context, String packageName, String owner, List<Term> terms) {

    public record Term(String term, String definition, List<Confusable> notToBeConfusedWith, List<Avoided> avoid) {
    }

    public record Confusable(String term, String context) {
    }

    public record Avoided(String word, String use, String reason, String decision) {
    }

    @SuppressWarnings("unchecked")
    public static Glossary load(Path file) {
        try (InputStream in = Files.newInputStream(file)) {
            Map<String, Object> root = new Yaml().load(in);
            List<Map<String, Object>> rawTerms = (List<Map<String, Object>>) root.getOrDefault("terms", List.of());
            List<Term> terms = rawTerms.stream().map(t -> new Term(
                    (String) t.get("term"),
                    (String) t.get("definition"),
                    ((List<Map<String, Object>>) t.getOrDefault("not_to_be_confused_with", List.of())).stream()
                            .map(c -> new Confusable((String) c.get("term"), (String) c.get("context"))).toList(),
                    ((List<Map<String, Object>>) t.getOrDefault("avoid", List.of())).stream()
                            .map(a -> new Avoided((String) a.get("word"), (String) a.get("use"),
                                    (String) a.get("reason"), (String) a.get("decision"))).toList()
            )).toList();
            return new Glossary((String) root.get("context"), (String) root.get("package"), (String) root.get("owner"), terms);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read " + file, e);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<String> notResponsibleFor(Path boundaryFile) {
        if (!Files.exists(boundaryFile)) {
            return List.of();
        }
        try (InputStream in = Files.newInputStream(boundaryFile)) {
            Map<String, Object> root = new Yaml().load(in);
            return (List<String>) root.getOrDefault("not_responsible_for", List.of());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read " + boundaryFile, e);
        }
    }
}
