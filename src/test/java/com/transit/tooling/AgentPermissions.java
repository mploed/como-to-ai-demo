package com.transit.tooling;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Turns the context map into tool permissions for an agent working in one context.
 * Example format: Claude Code project settings (.claude/settings.json). Other agents have
 * similar mechanisms, the idea is the same: permissions, not politeness.
 *
 * deny: other contexts' code and knowledge, and the agreed scenarios of our own context.
 * ask:  our published language, the model, the glossary and the checks themselves.
 */
public final class AgentPermissions {

    private AgentPermissions() {
    }

    public static String generate(String context, CmlModel model) {
        String folder = CmlModel.folderOf(context);
        String pkgPath = "src/main/java/" + CmlModel.packageOf(context).replace('.', '/');

        List<String> deny = new ArrayList<>();
        deny.add("Edit(/contexts/" + folder + "/scenarios/**)");
        for (String other : model.contexts()) {
            if (!other.equals(context)) {
                deny.add("Edit(/src/main/java/" + CmlModel.packageOf(other).replace('.', '/') + "/**)");
                deny.add("Edit(/contexts/" + CmlModel.folderOf(other) + "/**)");
            }
        }

        List<String> ask = new ArrayList<>();
        boolean publishesLanguage = model.relationships().stream()
                .anyMatch(r -> r.upstream().equals(context) && (r.upstreamOffers("OHS") || r.upstreamOffers("PL")));
        if (publishesLanguage) {
            ask.add("Edit(/" + pkgPath + "/api/**)");
        }
        ask.add("Edit(/model/**)");
        ask.add("Edit(/contexts/" + folder + "/glossary.yaml)");
        ask.add("Edit(/contexts/" + folder + "/boundary.yaml)");
        ask.add("Edit(/contexts/" + folder + "/decisions/**)");
        ask.add("Edit(/src/test/java/com/transit/architecture/**)");
        ask.add("Edit(/src/test/java/com/transit/language/**)");
        ask.add("Edit(/src/test/java/com/transit/tooling/**)");

        return "{\n"
                + "  \"permissions\": {\n"
                + "    \"deny\": [\n" + lines(deny) + "\n    ],\n"
                + "    \"ask\": [\n" + lines(ask) + "\n    ]\n"
                + "  }\n"
                + "}\n";
    }

    private static String lines(List<String> rules) {
        return rules.stream().map(r -> "      \"" + r + "\"").collect(Collectors.joining(",\n"));
    }
}
