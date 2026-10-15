package com.transit.architecture;

import com.transit.tooling.CmlModel;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideOutsideOfPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * The strategic relationships of model/transit.cml, executed as ArchUnit rules.
 * Change the context map, and the build changes with it. No hand written rule per pair.
 *
 * Prototype: Context Mapper's own ArchUnit extension checks tactical elements today,
 * this test sketches the bridge for the strategic part.
 */
class ContextMapRulesTest {

    private static final CmlModel MODEL = CmlModel.load(Path.of("model/transit.cml"));
    private static final JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.transit");

    @TestFactory
    List<DynamicTest> context_map_is_enforced() {
        List<DynamicTest> tests = new ArrayList<>();
        for (CmlModel.Relationship r : MODEL.relationships()) {
            String up = CmlModel.packageOf(r.upstream());
            String down = CmlModel.packageOf(r.downstream());
            if (r.symmetric()) {
                continue; // Partnership and Shared Kernel are agreements between teams, not dependency rules.
            }

            tests.add(test(r, r.upstream() + " never depends on its downstream " + r.downstream(),
                    noClasses().that().resideInAPackage(up + "..")
                            .should().dependOnClassesThat().resideInAPackage(down + "..")));

            if (r.upstreamOffers("OHS") || r.upstreamOffers("PL")) {
                tests.add(test(r, r.downstream() + " uses only the published language " + up + ".api",
                        noClasses().that().resideInAPackage(down + "..")
                                .should().dependOnClassesThat(resideInAPackage(up + "..")
                                        .and(resideOutsideOfPackage(up + ".api..")))));
            }

            if (r.downstreamUses("ACL")) {
                tests.add(test(r, "only the anticorruption layer of " + r.downstream() + " talks to " + r.upstream(),
                        noClasses().that().resideInAPackage(down + "..")
                                .and().resideOutsideOfPackage(down + "..acl..")
                                .should().dependOnClassesThat().resideInAPackage(up + "..")));
            }
        }

        for (List<String> pair : MODEL.separateWays()) {
            String a = CmlModel.packageOf(pair.get(0));
            String b = CmlModel.packageOf(pair.get(1));
            String line = pair.get(0) + " and " + pair.get(1) + " have no relationship in the context map";
            tests.add(DynamicTest.dynamicTest("Separate Ways: " + line, () -> {
                noClasses().that().resideInAPackage(a + "..").should().dependOnClassesThat().resideInAPackage(b + "..")
                        .because(line).check(CLASSES);
                noClasses().that().resideInAPackage(b + "..").should().dependOnClassesThat().resideInAPackage(a + "..")
                        .because(line).check(CLASSES);
            }));
        }
        return tests;
    }

    private static DynamicTest test(CmlModel.Relationship r, String name, ArchRule rule) {
        return DynamicTest.dynamicTest(name + "  [" + r.line() + "]",
                () -> rule.because("the context map says: " + r.line()).check(CLASSES));
    }
}
