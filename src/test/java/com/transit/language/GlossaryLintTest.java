package com.transit.language;

import com.transit.tooling.Glossary;
import com.transit.tooling.GlossaryLint;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

/** The words we decided against in the modeling session must not come back through generated code. */
class GlossaryLintTest {

    @Test
    void ticket_sales_speaks_its_ubiquitous_language() {
        Path root = Path.of(".").toAbsolutePath().normalize();
        Glossary glossary = Glossary.load(root.resolve("contexts/ticket-sales/glossary.yaml"));
        List<String> violations = GlossaryLint.check(glossary, root.resolve("src/main/java"), root);
        if (!violations.isEmpty()) {
            fail("Ubiquitous Language violated in " + glossary.context() + ":\n\n" + String.join("\n\n", violations));
        }
    }
}
