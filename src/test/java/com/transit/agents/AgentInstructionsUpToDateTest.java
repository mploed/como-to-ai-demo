package com.transit.agents;

import com.transit.tooling.AgentInstructions;
import com.transit.tooling.AgentPermissions;
import com.transit.tooling.CmlModel;
import com.transit.tooling.GenerateAgentInstructions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** AGENTS.md and the agent permissions are generated from the model. If the model changes, both must follow. */
class AgentInstructionsUpToDateTest {

    @Test
    void agents_md_matches_the_model() throws Exception {
        Path root = Path.of(".");
        String committed = Files.readString(root.resolve("contexts/ticket-sales/AGENTS.md"));
        assertEquals(AgentInstructions.generate("TicketSales", root), committed,
                "contexts/ticket-sales/AGENTS.md is stale. Run: mvn -q test-compile exec:java@agents");
    }

    @Test
    void agent_permissions_match_the_model() throws Exception {
        Path root = Path.of(".");
        String committed = Files.readString(root.resolve(".claude/settings.json"));
        CmlModel model = CmlModel.load(root.resolve("model/transit.cml"));
        assertEquals(AgentPermissions.generate(GenerateAgentInstructions.AGENT_CONTEXT, model), committed,
                ".claude/settings.json is stale. Run: mvn -q test-compile exec:java@agents");
    }
}
