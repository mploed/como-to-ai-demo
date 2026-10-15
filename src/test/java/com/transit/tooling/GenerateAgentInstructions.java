package com.transit.tooling;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Regenerates contexts/<context>/AGENTS.md for every context that has a glossary,
 * and .claude/settings.json (agent permissions) for the context this repository's agents work in.
 */
public final class GenerateAgentInstructions {

    /** The context whose team runs agents in this repository. */
    public static final String AGENT_CONTEXT = "TicketSales";

    private GenerateAgentInstructions() {
    }

    public static void main(String[] args) throws IOException {
        Path root = Path.of(args.length > 0 ? args[0] : ".");
        CmlModel model = CmlModel.load(root.resolve("model/transit.cml"));
        for (String context : model.contexts()) {
            Path dir = root.resolve("contexts").resolve(CmlModel.folderOf(context));
            if (Files.exists(dir.resolve("glossary.yaml"))) {
                Path target = dir.resolve("AGENTS.md");
                Files.writeString(target, AgentInstructions.generate(context, root));
                System.out.println("Generated " + target);
            }
        }
        Path settings = root.resolve(".claude/settings.json");
        Files.createDirectories(settings.getParent());
        Files.writeString(settings, AgentPermissions.generate(AGENT_CONTEXT, model));
        System.out.println("Generated " + settings);
    }
}
