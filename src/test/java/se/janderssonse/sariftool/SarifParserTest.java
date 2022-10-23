// SPDX-FileCopyrightText: 2021 Baloise Group
// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sariftool;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import se.janderssonse.sariftool.model.sarif.Driver;
import se.janderssonse.sariftool.model.sarif.Location;
import se.janderssonse.sariftool.model.sarif.Region;
import se.janderssonse.sariftool.model.sarif.Result;
import se.janderssonse.sariftool.model.sarif.Rule;
import se.janderssonse.sariftool.model.sarif.RuleProperties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class SarifParserTest {

    @Test
    void when_invalid_sarif_given_parsing_did_not_occur() throws URISyntaxException, IOException {

        final Path sarifFile = Paths.get(ClassLoader.getSystemResource("ajsonfile.json").toURI());
        SarifParser parser = new SarifParser(sarifFile);

        assertFalse(parser.validated());
        assertTrue(parser.getSchema().isEmpty());
        assertTrue(parser.getVersion().isEmpty());

    }

    @Test
    void when_nonexisting_file_given_parsing_did_not_occur() {

        SarifParser parser = new SarifParser(Paths.get("null"));

        assertFalse(parser.validated());
        assertTrue(parser.getSchema().isEmpty());
        assertTrue(parser.getVersion().isEmpty());
        assertTrue(parser.getResults().isEmpty());
    }

    @Test
    void when_valid_sariffile_given_it_was_parsed() throws Exception {

        final Path sarifFile = Paths.get(ClassLoader.getSystemResource("exampleWithTestDir.sarif").toURI());
        SarifParser parser = new SarifParser(sarifFile);

        assertTrue(parser.validated());
        assertEquals("https://raw.githubusercontent.com/oasis-tcs/sarif-spec/master/Schemata/sarif-schema-2.1.0.json",
                parser.getSchema().get());
        assertEquals(2, parser.getResults().size());

        assertDriver("2.3.3", parser.getDriver().get());
        assertRuleEmptySynchBlock(parser.getRules().get(2));
        assertRuleImpossibleArrayCast(parser.getRules().get(3));
        assertResult(parser.getResults().get(0));
    }

    @Test
    void when_valid_sariffile_with_rules_in_extension_given_it_was_parsed() throws Exception {

        final Path sarifFile = Paths.get(ClassLoader.getSystemResource("rulesInExtensions.sarif").toURI());
        SarifParser parser = new SarifParser(sarifFile);

        assertEquals("2.1.0", parser.getVersion().get());

        assertEquals(
                "https://raw.githubusercontent.com/oasis-tcs/sarif-spec/master/Schemata/sarif-schema-2.1.0.json",
                parser.getSchema().get());

        assertDriver("2.5.5", parser.getDriver().get());

        assertEquals(166, parser.getRules().size());

        final Rule sqlInjectionRule = parser.getRules().get(0);
        assertEquals("java/sql-injection", sqlInjectionRule.id().get());
        assertEquals("java/sql-injection", sqlInjectionRule.name().get());
        assertEquals("Query built from user-controlled sources",
                sqlInjectionRule.shortDescription().get());
        assertEquals(
                "Building a SQL or Java Persistence query from user-controlled sources is vulnerable to insertion of malicious code by the user.",
                sqlInjectionRule.fullDescription().get());
        assertEquals(Rule.Level.ERROR, sqlInjectionRule.level().get());

        final RuleProperties ruleProperties = sqlInjectionRule.properties().get();
        assertEquals("java/sql-injection", ruleProperties.id().get());
        assertEquals("Query built from user-controlled sources",
                ruleProperties.name().get());
        assertEquals(
                "Building a SQL or Java Persistence query from user-controlled sources is vulnerable to insertion of\n              malicious code by the user.",
                ruleProperties.description().get());
        assertEquals("high", ruleProperties.precision().get());
        assertEquals("path-problem", ruleProperties.kind().get());
        assertEquals(RuleProperties.Severity.error, ruleProperties.severity().get());

        final List<String> esbTags = ruleProperties.tags().get();
        assertEquals(3, esbTags.size());
        assertEquals(1, esbTags.stream().filter("security"::equals).count());
        assertEquals(1,
                esbTags.stream().filter("external/cwe/cwe-089"::equals).count());
        assertEquals(1,
                esbTags.stream().filter("external/cwe/cwe-564"::equals).count());

        final Result result = parser.getResults().get(0);
        assertEquals("java/input-resource-leak", result.ruleId().get());
        assertEquals(64, result.ruleIndex().get());
        assertEquals("This FileReader is not always closed on method exit.",
                result.message().get());
        assertEquals(1, result.locations().get().size());

        final Location location = result.locations().get().get(0);
        assertEquals(
                "src/main/java/com/baloise/open/maven/codeql/sarif/SarifParser.java",
                location.uri());
        assertEquals("%SRCROOT%", location.uriBaseId().get());
        assertEquals(0, location.index().get());

        final Region region = location.region().get();
        assertEquals(84, region.startLine());
        assertEquals(58, region.startColumn().get());
        assertEquals(88, region.endColumn().get());
    }

    private void assertResult(Result result) {

        assertEquals("java/misleading-indentation", result.ruleId().get());
        assertEquals(9, result.ruleIndex().get());
        assertEquals(
                "Indentation suggests that [the next statement](1) belongs to [the control structure](2), but this is not the case; consider adding braces or adjusting indentation.",
                result.message().get());
        assertEquals(1, result.locations().get().size());

        final Location location = result.locations().get().get(0);
        assertEquals(
                "src/main/java/org/arburk/fishbone/infrastructure/service/FishRepository.java",
                location.uri());
        assertEquals("%SRCROOT%", location.uriBaseId().get());
        assertEquals(0, location.index().get());

        final Region region = location.region().get();
        assertEquals(26, region.startLine());
        assertEquals(9, region.startColumn().get());
        assertEquals(13, region.endColumn().get());
    }

    private void assertDriver(String version, Driver driver) {

        assertEquals("GitHub", driver.organization().get());
        assertEquals("CodeQL", driver.name().get());
        assertEquals(version, driver.semanticVersion().get());
    }

    private void assertRuleImpossibleArrayCast(Rule impossibleArrayCast) {

        assertEquals("java/impossible-array-cast", impossibleArrayCast.id().get());
        assertEquals("java/impossible-array-cast", impossibleArrayCast.name().get());
        assertEquals("Impossible array cast",
                impossibleArrayCast.shortDescription().get());
        assertEquals(
                "Trying to cast an array of a particular type as an array of a subtype causes a 'ClassCastException' at runtime.",
                impossibleArrayCast.fullDescription().get());
        assertNotNull(impossibleArrayCast.level());
        assertEquals(Rule.Level.ERROR, impossibleArrayCast.level().get());

        final RuleProperties icProperties = impossibleArrayCast.properties().get();
        assertEquals("java/impossible-array-cast", icProperties.id().get());
        assertEquals("Impossible array cast", icProperties.name().get());
        assertEquals(
                "Trying to cast an array of a particular type as an array of a subtype causes a\n              'ClassCastException' at runtime.",
                icProperties.description().get());
        assertEquals("low", icProperties.precision().get());
        assertEquals("problem", icProperties.kind().get());
        assertEquals(RuleProperties.Severity.error, icProperties.severity().get());

        final List<String> icTags = icProperties.tags().get();
        assertEquals(4, icTags.size());
        assertEquals(1, icTags.stream().filter("reliability"::equals).count());
        assertEquals(1, icTags.stream().filter("correctness"::equals).count());
        assertEquals(1, icTags.stream().filter("logic"::equals).count());
        assertEquals(1,
                icTags.stream().filter("external/cwe/cwe-704"::equals).count());
    }

    private void assertRuleEmptySynchBlock(Rule emptySynchBlock) {

        assertEquals("java/empty-synchronized-block", emptySynchBlock.id().get());
        assertEquals("java/empty-synchronized-block", emptySynchBlock.name().get());
        assertEquals("Empty synchronized block",
                emptySynchBlock.shortDescription().get());
        assertEquals(
                "Empty synchronized blocks may indicate the presence of incomplete code or incorrect synchronization, and may lead to concurrency problems.",
                emptySynchBlock.fullDescription().get());
        assertEquals(emptySynchBlock.level(), Optional.empty());

        final RuleProperties esbProperties = emptySynchBlock.properties().get();
        assertEquals("java/empty-synchronized-block", esbProperties.id().get());
        assertEquals("Empty synchronized block", esbProperties.name().get());
        assertEquals(
                "Empty synchronized blocks may indicate the presence of\n              incomplete code or incorrect synchronization, and may lead to concurrency problems.",
                esbProperties.description().get());
        assertEquals("low", esbProperties.precision().get());
        assertEquals("problem", esbProperties.kind().get());
        assertEquals(RuleProperties.Severity.warning,
                esbProperties.severity().get());

        final List<String> esbTags = esbProperties.tags().get();
        assertEquals(5, esbTags.size());
        assertEquals(1, esbTags.stream().filter("reliability"::equals).count());
        assertEquals(1, esbTags.stream().filter("correctness"::equals).count());
        assertEquals(1, esbTags.stream().filter("concurrency"::equals).count());
        assertEquals(1,
                esbTags.stream().filter("language-features"::equals).count());
        assertEquals(1,
                esbTags.stream().filter("external/cwe/cwe-585"::equals).count());
    }
}
