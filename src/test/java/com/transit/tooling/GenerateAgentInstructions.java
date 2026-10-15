package com.transit.tooling;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Regenerates contexts/<context>/AGENTS.md for every context that has a glossary. */
public final class GenerateAgentInstructions {

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
    }
}
