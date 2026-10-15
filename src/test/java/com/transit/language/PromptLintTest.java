package com.transit.language;

import com.transit.tooling.PromptLint;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The vague prompt from the opening of the talk, linted before anything gets built. */
class PromptLintTest {

    @Test
    void the_vague_prompt_gets_questions_back() {
        List<String> questions = PromptLint.questionsFor("Implement ticket cancellation for the mobile app", Path.of("."));
        assertEquals(2, questions.size(), String.join("\n", questions));
        assertTrue(questions.get(0).startsWith("'Ticket' in which context?"));
        assertTrue(questions.get(1).startsWith("'Cancellation' is not a term in ticket-sales. Do you mean Refund?"));
    }

    @Test
    void a_precise_prompt_passes() {
        assertTrue(PromptLint.questionsFor("Implement partial refunds for fare products", Path.of(".")).isEmpty());
    }
}
