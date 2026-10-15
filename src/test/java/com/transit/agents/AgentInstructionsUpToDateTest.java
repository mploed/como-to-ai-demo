package com.transit.agents;

import com.transit.tooling.AgentInstructions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** AGENTS.md is generated from the model. If someone edits the model, the instructions must follow. */
class AgentInstructionsUpToDateTest {

    @Test
    void agents_md_matches_the_model() throws Exception {
        Path root = Path.of(".");
        String committed = Files.readString(root.resolve("contexts/ticket-sales/AGENTS.md"));
        assertEquals(AgentInstructions.generate("TicketSales", root), committed,
                "contexts/ticket-sales/AGENTS.md is stale. Run: mvn -q test-compile exec:java");
    }
}
