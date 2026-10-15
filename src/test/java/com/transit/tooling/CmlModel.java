package com.transit.tooling;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Deliberately small reader for the parts of a CML file this demo needs:
 * bounded contexts, subdomains and the relationships of the context map.
 * Prototype only. A production version would use the Context Mapper parser.
 */
public record CmlModel(List<String> contexts,
                       Map<String, BoundedContext> boundedContexts,
                       Map<String, String> subdomainTypes,
                       List<Relationship> relationships) {

    public record BoundedContext(String name, String implementsSubdomain, String vision, List<String> responsibilities) {
    }

    public record Relationship(String upstream, List<String> upstreamRoles,
                               String downstream, List<String> downstreamRoles,
                               boolean symmetric, String line) {

        public boolean upstreamOffers(String role) {
            return upstreamRoles.contains(role);
        }

        public boolean downstreamUses(String role) {
            return downstreamRoles.contains(role);
        }

        public boolean involves(String context) {
            return upstream.equals(context) || downstream.equals(context);
        }
    }

    private static final Pattern RELATIONSHIP = Pattern.compile(
            "^\\s*(\\w+)\\s*\\[([^\\]]*)\\]\\s*(<->|->|<-)\\s*\\[([^\\]]*)\\]\\s*(\\w+)", Pattern.MULTILINE);
    private static final Pattern CONTAINS = Pattern.compile("contains\\s+([\\w\\s,]+?)\\s*\\n");
    private static final Pattern BOUNDED_CONTEXT = Pattern.compile("BoundedContext\\s+(\\w+)(?:\\s+implements\\s+(\\w+))?\\s*\\{");
    private static final Pattern SUBDOMAIN = Pattern.compile("Subdomain\\s+(\\w+)\\s*\\{([^}]*)\\}");
    private static final Pattern TYPE = Pattern.compile("type\\s*=\\s*(\\w+)");
    private static final Pattern VISION = Pattern.compile("domainVisionStatement\\s*=\\s*\"([^\"]*)\"");
    private static final Pattern RESPONSIBILITIES = Pattern.compile("responsibilities\\s*=\\s*((?:\"[^\"]*\"\\s*,?\\s*)+)");
    private static final Pattern QUOTED = Pattern.compile("\"([^\"]*)\"");

    public static CmlModel load(Path file) {
        try {
            return parse(Files.readString(file));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read " + file, e);
        }
    }

    public static CmlModel parse(String cml) {
        String text = cml.replaceAll("(?s)/\\*.*?\\*/", "").replaceAll("//[^\\n]*", "");

        List<String> contexts = new ArrayList<>();
        Matcher contains = CONTAINS.matcher(text);
        if (contains.find()) {
            Arrays.stream(contains.group(1).split(",")).map(String::trim).filter(s -> !s.isEmpty()).forEach(contexts::add);
        }

        Map<String, String> subdomainTypes = new LinkedHashMap<>();
        Matcher subdomain = SUBDOMAIN.matcher(text);
        while (subdomain.find()) {
            Matcher type = TYPE.matcher(subdomain.group(2));
            subdomainTypes.put(subdomain.group(1), type.find() ? type.group(1) : "UNDEFINED");
        }

        Map<String, BoundedContext> boundedContexts = new LinkedHashMap<>();
        Matcher bc = BOUNDED_CONTEXT.matcher(text);
        while (bc.find()) {
            String body = blockAfter(text, bc.end() - 1);
            Matcher vision = VISION.matcher(body);
            Matcher responsibilities = RESPONSIBILITIES.matcher(body);
            List<String> resp = new ArrayList<>();
            if (responsibilities.find()) {
                Matcher q = QUOTED.matcher(responsibilities.group(1));
                while (q.find()) {
                    resp.add(q.group(1));
                }
            }
            boundedContexts.put(bc.group(1), new BoundedContext(bc.group(1), bc.group(2),
                    vision.find() ? vision.group(1) : "", List.copyOf(resp)));
        }

        List<Relationship> relationships = new ArrayList<>();
        Matcher rel = RELATIONSHIP.matcher(text);
        while (rel.find()) {
            String left = rel.group(1);
            List<String> leftRoles = roles(rel.group(2));
            String arrow = rel.group(3);
            List<String> rightRoles = roles(rel.group(4));
            String right = rel.group(5);
            String line = rel.group(0).trim();
            switch (arrow) {
                case "->" -> relationships.add(new Relationship(left, leftRoles, right, rightRoles, false, line));
                case "<-" -> relationships.add(new Relationship(right, rightRoles, left, leftRoles, false, line));
                default -> relationships.add(new Relationship(left, leftRoles, right, rightRoles, true, line));
            }
        }
        return new CmlModel(List.copyOf(contexts), boundedContexts, subdomainTypes, List.copyOf(relationships));
    }

    private static String blockAfter(String text, int openingBrace) {
        int depth = 0;
        for (int i = openingBrace; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return text.substring(openingBrace + 1, i);
                }
            }
        }
        throw new IllegalArgumentException("Unbalanced braces in CML");
    }

    private static List<String> roles(String raw) {
        return Arrays.stream(raw.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    /** Convention of this repository: TicketSales lives in com.transit.ticketsales. */
    public static String packageOf(String context) {
        return "com.transit." + context.toLowerCase(Locale.ROOT);
    }

    /** Convention of this repository: TicketSales has its knowledge in contexts/ticket-sales. */
    public static String folderOf(String context) {
        return context.replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase(Locale.ROOT);
    }

    public List<Relationship> relationshipsOf(String context) {
        return relationships.stream().filter(r -> r.involves(context)).toList();
    }

    /** Contexts in the map without any relationship: Separate Ways, no dependency allowed. */
    public List<List<String>> separateWays() {
        List<List<String>> pairs = new ArrayList<>();
        for (int i = 0; i < contexts.size(); i++) {
            for (int j = i + 1; j < contexts.size(); j++) {
                String a = contexts.get(i);
                String b = contexts.get(j);
                boolean related = relationships.stream().anyMatch(r -> r.involves(a) && r.involves(b));
                if (!related) {
                    pairs.add(List.of(a, b));
                }
            }
        }
        return pairs;
    }

    public Optional<String> subdomainTypeOf(String context) {
        BoundedContext bc = boundedContexts.get(context);
        if (bc == null || bc.implementsSubdomain() == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(subdomainTypes.get(bc.implementsSubdomain()));
    }
}
