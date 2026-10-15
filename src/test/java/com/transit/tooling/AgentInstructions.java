package com.transit.tooling;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generates the AGENTS.md of a Bounded Context from the same sources the build checks:
 * the CML context map, the glossary and the boundary file. One model, many readers.
 */
public final class AgentInstructions {

    private AgentInstructions() {
    }

    public static String generate(String context, Path repoRoot) {
        CmlModel model = CmlModel.load(repoRoot.resolve("model/transit.cml"));
        String folder = CmlModel.folderOf(context);
        Path contextDir = repoRoot.resolve("contexts").resolve(folder);
        Glossary glossary = Glossary.load(contextDir.resolve("glossary.yaml"));
        List<String> notResponsibleFor = Glossary.notResponsibleFor(contextDir.resolve("boundary.yaml"));
        CmlModel.BoundedContext bc = model.boundedContexts().get(context);
        String pkg = CmlModel.packageOf(context);

        StringBuilder md = new StringBuilder();
        md.append("<!-- GENERATED from model/transit.cml, contexts/").append(folder).append("/glossary.yaml and boundary.yaml.\n")
          .append("     Do not edit by hand. Regenerate with: mvn -q test-compile exec:java -->\n\n");
        md.append("# Agent instructions: ").append(context).append("\n\n");
        md.append(bc.vision()).append("\n\n");
        md.append("- Subdomain: ").append(bc.implementsSubdomain())
          .append(" (").append(model.subdomainTypeOf(context).orElse("UNDEFINED")).append(")\n");
        md.append("- Code: `").append(pkg).append("`\n");
        md.append("- Owner of the language: ").append(glossary.owner()).append("\n\n");

        md.append("## Responsible for\n\n");
        bc.responsibilities().forEach(r -> md.append("- ").append(r).append("\n"));
        md.append("\n## Not responsible for\n\n");
        notResponsibleFor.forEach(r -> md.append("- ").append(r).append("\n"));

        md.append("\n## Language\n\n");
        for (Glossary.Term term : glossary.terms()) {
            md.append("- **").append(term.term()).append("**: ").append(term.definition());
            if (!term.notToBeConfusedWith().isEmpty()) {
                md.append(" Not to be confused with: ").append(term.notToBeConfusedWith().stream()
                        .map(c -> c.term() + " (" + c.context() + ")").collect(Collectors.joining(", "))).append(".");
            }
            if (!term.avoid().isEmpty()) {
                md.append(" Never say: ").append(term.avoid().stream()
                        .map(Glossary.Avoided::word).collect(Collectors.joining(", "))).append(".");
            }
            md.append("\n");
        }

        md.append("\n## Policies from the context map\n\n");
        for (CmlModel.Relationship r : model.relationshipsOf(context)) {
            md.append("- `").append(r.line()).append("`: ").append(policy(context, r)).append("\n");
        }
        for (List<String> pair : model.separateWays()) {
            if (pair.contains(context)) {
                String other = pair.get(0).equals(context) ? pair.get(1) : pair.get(0);
                md.append("- No relationship to ").append(other)
                  .append(" (Separate Ways): no dependency on `").append(CmlModel.packageOf(other)).append("`. [build]\n");
            }
        }

        md.append("\n## How to work here\n\n")
          .append("- First name the Bounded Context of the request. If the request is ambiguous (\"ticket\"), ask before you do anything else.\n")
          .append("- Examples first: draft scenarios and invariants for one slice and wait until the domain expert agrees.\n")
          .append("- Agreed scenarios in `contexts/").append(folder).append("/scenarios` are read only. Propose new ones, never edit agreed ones.\n")
          .append("- Unknown term or uncovered case: do not invent. Add a question to `contexts/").append(folder).append("/open-questions.md` and continue with the rest.\n")
          .append("- The build checks these rules: `GlossaryLintTest`, `ContextMapRulesTest`, `TicketRefundProperties`, `RefundScenarioTest`.\n");
        return md.toString();
    }

    private static String policy(String self, CmlModel.Relationship r) {
        String up = r.upstream();
        String down = r.downstream();
        if (r.symmetric()) {
            String other = up.equals(self) ? down : up;
            if (r.upstreamOffers("SK")) {
                return "Shared Kernel with " + other + ". Changes only with approval of both owners, never autonomous. [code owners]";
            }
            return "Partnership with " + other + ". Tasks across both contexts only when planned jointly. [agreement]";
        }
        if (up.equals(self)) {
            String text = down + " depends on you.";
            if (r.upstreamOffers("OHS") || r.upstreamOffers("PL")) {
                text += " Your published language is `" + CmlModel.packageOf(self) + ".api`. Changing it needs the "
                        + down + " owner.";
            }
            return text + " Never depend on " + down + ". [permission, build]";
        }
        StringBuilder text = new StringBuilder("You depend on ").append(up).append(".");
        if (r.downstreamUses("ACL")) {
            text.append(" Only `").append(CmlModel.packageOf(self)).append("..acl..` may use ").append(up)
                .append(". Translate into your own language there, nothing behind the anticorruption layer sees ")
                .append(up).append(" types. [build]");
        } else if (r.downstreamUses("CF")) {
            text.append(" Conformist: use the upstream model as is, no own translation. [agreement]");
        }
        if (r.upstreamOffers("OHS")) {
            text.append(" Use only its published API `").append(CmlModel.packageOf(up)).append(".api`. [build]");
        }
        if (r.downstreamUses("C")) {
            text.append(" Customer/Supplier: draft a request for the upstream team, never change upstream code. [permission]");
        }
        return text.toString();
    }
}
