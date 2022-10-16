// SPDX-FileCopyrightText: 2021 Baloise Group
// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sariftool.mapper;

import se.janderssonse.sariftool.model.sarif.Driver;
import se.janderssonse.sariftool.model.sarif.Location;
import se.janderssonse.sariftool.model.sarif.Region;
import se.janderssonse.sariftool.model.sarif.Result;
import se.janderssonse.sariftool.model.sarif.Rule;
import se.janderssonse.sariftool.model.sarif.RuleProperties;
import se.janderssonse.sariftool.model.sarif.RuleProperties.Severity;
import se.janderssonse.sariftool.model.sonar.Issue;
import se.janderssonse.sariftool.model.sonar.Issues;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

class SonarMapperTest {

    public static final String TEST_RULE_ID = "testRuleId";
    public static final String TEST_URI_BASE_ID = "testUriBaseId";

    @Test
    void testParserCallback_onFinding() {
        final SonarMapper testee = new SonarMapper();

        assertEquals("parsed 0 Rules, 0 Results resulting in 0 issues.", testee.summary());
        assertEquals(0, testee.getMappedIssues(null).getResult().size());
        assertEquals(0, testee.getMappedIssues(new String[0]).getResult().size());

        testee.onFinding(null);
        assertEquals("parsed 0 Rules, 0 Results resulting in 0 issues.", testee.summary());
        assertEquals(0, testee.getMappedIssues(null).getResult().size());
        assertEquals(0, testee.getMappedIssues(new String[0]).getResult().size());

        testee.onFinding(createTestResult("testUri"));

        assertEquals("parsed 0 Rules, 1 Results resulting in 1 issues.", testee.summary());
        assertEquals(1, testee.getMappedIssues(null).getResult().size());
        assertEquals(1, testee.getMappedIssues(new String[0]).getResult().size());
    }

    @Test
    void testMapperInjected() {
        RuleProperties properties = createRuleProperties(Severity.error);
        final SonarMapper testee = new SonarMapper();
        testee.setDriver(new Driver(Optional.of("driverName"),
                Optional.of("DriverOrg"),
                Optional.of("DriverVersion")));
        testee.setRules(List.of(createTestRule(properties)));

        testee.onFinding(createTestResult("testUri"));
        assertEquals("parsed 1 Rules, 1 Results resulting in 1 issues.", testee.summary());
        assertEquals(1, testee.getMappedIssues(null).getResult().size());

        final Issue issue = testee.getMappedIssues(null).getResult().get(0);
        assertEquals(TEST_RULE_ID, issue.ruleId().get());
        assertEquals(Issue.Severity.BLOCKER, issue.severity().get());
        assertNotNull(issue.primaryLocation().get());
        assertEquals(issue.secondaryLocations(), Optional.empty());
        assertEquals("DriverOrg driverName vDriverVersion", issue.engineId().get());
    }

    @Test
    void testMappedIssues_FilterResults() {
        RuleProperties properties = createRuleProperties(Severity.error);
        final SonarMapper testee = new SonarMapper();
        testee.setDriver(new Driver(Optional.of("driverName"), Optional.of("DriverOrg"),
                Optional.of("DriverVersion")));
        testee.setRules(List.of(createTestRule(properties)));

        testee.onFinding(createTestResult("src/test/java/MyTestClass.java"));
        testee.onFinding(createTestResult("src/main/java/mypackage/MyClass.java"));

        assertEquals(2, testee.getMappedIssues(null).getResult().size());
        assertMatchingIssue(testee, new String[] { "/test/" }, "src/main/java/mypackage/MyClass.java");
        assertMatchingIssue(testee, new String[] { "/TEST/" }, "src/main/java/mypackage/MyClass.java");
        assertMatchingIssue(testee, new String[] { "mypackage" }, "src/test/java/MyTestClass.java");
        assertMatchingIssue(testee, new String[] { "mypackage" }, "src/test/java/MyTestClass.java");

        testee.onFinding(createTestResult("src/main/java/another/package/AnyTester.java"));
        assertEquals(3, testee.getMappedIssues(null).getResult().size());
        assertMatchingIssue(testee, new String[] { "mypackage", "another/package" },
                "src/test/java/MyTestClass.java");
        assertMatchingIssue(testee, new String[] { "(test)" }, "src/main/java/mypackage/MyClass.java");
        assertMatchingIssue(testee, new String[] { "(my[\\S]*\\.java)" },
                "src/main/java/another/package/AnyTester.java");
    }

    private void assertMatchingIssue(SonarMapper testee, String[] patternsToExclude, String expected1stPath) {
        final Issues mappedIssuesFiltered = testee.getMappedIssues(patternsToExclude);
        assertEquals(1, mappedIssuesFiltered.getResult().size());
        assertEquals(expected1stPath,
                mappedIssuesFiltered.getResult().get(0).primaryLocation().get().filePath());
    }

    @Test
    void testParserCallback_onVersion() {
        final SonarMapper testee = new SonarMapper();
        assertNull(testee.getVersion());
        testee.onVersion("AnyVersion");
        assertEquals("AnyVersion", testee.getVersion());
    }

    @Test
    void testParserCallback_onSchema() {
        final SonarMapper testee = new SonarMapper();
        assertNull(testee.getSchema());
        testee.onSchema("AnySchema");
        assertEquals("AnySchema", testee.getSchema());
    }

    @Test
    void testParserCallback_onRule() {
        final SonarMapper testee = new SonarMapper();
        assertEquals("parsed 0 Rules, 0 Results resulting in 0 issues.", testee.summary());
        RuleProperties properties = createRuleProperties(Severity.error);
        testee.onRule(createTestRule(properties));
        assertEquals("parsed 1 Rules, 0 Results resulting in 0 issues.", testee.summary());
    }

    @Test
    void testMapSeverity_noMatch_ReturnInfo() {
        final SonarMapper testee = new SonarMapper();
        assertEquals(Issue.Severity.INFO, testee.mapSeverity(null));

        RuleProperties props = new RuleProperties(
                Optional.of(TEST_RULE_ID), Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty());
        final Rule testRule = createTestRule(props);

        testee.onRule(testRule);

        assertEquals(Issue.Severity.INFO, testee.mapSeverity(TEST_RULE_ID));
    }

    @Test
    void testMapRuleToIssueSeverity_LenientInput() {
        final SonarMapper testee = new SonarMapper();
        Optional<RuleProperties> rp = Optional.of(new RuleProperties(Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty()));
        assertNull(testee.mapRuleToIssueSeverity(null, Optional.empty()));
        assertNull(testee.mapRuleToIssueSeverity(null, rp));
        assertEquals(Issue.Severity.MINOR, testee.mapRuleToIssueSeverity(Rule.Level.NONE,
                rp));
        assertEquals(Issue.Severity.MINOR, testee.mapRuleToIssueSeverity(Rule.Level.NOTE,
                rp));
        assertEquals(Issue.Severity.MAJOR, testee.mapRuleToIssueSeverity(Rule.Level.WARNING,
                rp));
        assertEquals(Issue.Severity.CRITICAL, testee.mapRuleToIssueSeverity(Rule.Level.ERROR,
                rp));
    }

    /*
     * @Test
     * void testMapRuleToIssueSeverity() {
     * final SonarIssueMapper testee = new SonarIssueMapper();
     *
     * assertNull(testee.mapRuleToIssueSeverity(null, Optional.empty()));
     * assertEquals(ImmutableIssue.Severity.INFO,
     * testee.mapRuleToIssueSeverity(Rule.Level.NONE,
     * Optional.of(RuleProperties.builder()
     * .severity(RuleProperties.Severity.recommendation).build())));
     *
     * assertEquals(ImmutableIssue.Severity.MINOR,
     * testee.mapRuleToIssueSeverity(null,
     * Optional.of(RuleProperties.builder()
     * .severity(RuleProperties.Severity.warning).build())));
     * assertEquals(ImmutableIssue.Severity.MINOR,
     * testee.mapRuleToIssueSeverity(Rule.Level.NONE,
     * Optional.of(RuleProperties.builder()
     * .severity(RuleProperties.Severity.warning)
     * .precision("medium").build())));
     * assertEquals(ImmutableIssue.Severity.MAJOR,
     * testee.mapRuleToIssueSeverity(Rule.Level.NONE,
     * Optional.of(RuleProperties.builder()
     * .severity(RuleProperties.Severity.warning)
     * .precision("high").build())));
     * assertEquals(ImmutableIssue.Severity.CRITICAL,
     * testee.mapRuleToIssueSeverity(Rule.Level.NONE,
     * Optional.of(RuleProperties.builder()
     * .severity(RuleProperties.Severity.warning)
     * .precision("very-high").build())));
     * assertEquals(ImmutableIssue.Severity.MINOR,
     * testee.mapRuleToIssueSeverity(null,
     * Optional.of(RuleProperties.builder()
     * .severity(RuleProperties.Severity.warning)
     * .precision("<invalid>").build())));
     *
     * assertEquals(ImmutableIssue.Severity.CRITICAL,
     * testee.mapRuleToIssueSeverity(null,
     * Optional.of(ImmutableRuleProperties
     * .builder().severity(RuleProperties.Severity.error).build())));
     * assertEquals(ImmutableIssue.Severity.BLOCKER,
     * testee.mapRuleToIssueSeverity(Rule.Level.ERROR,
     * Optional.of(RuleProperties
     * .builder()
     * .severity(RuleProperties.Severity.error)
     * .build())));
     * assertEquals(ImmutableIssue.Severity.CRITICAL,
     * testee.mapRuleToIssueSeverity(Rule.Level.NONE,
     * Optional.of(RuleProperties
     * .builder()
     * .severity(RuleProperties.Severity.error)
     * .build())));
     * assertEquals(ImmutableIssue.Severity.CRITICAL,
     * testee.mapRuleToIssueSeverity(Rule.Level.NONE,
     * Optional.of(RuleProperties.builder()
     * .severity(RuleProperties.Severity.error)
     * .precision("medium").build())));
     * assertEquals(ImmutableIssue.Severity.CRITICAL,
     * testee.mapRuleToIssueSeverity(Rule.Level.NONE,
     * Optional.of(RuleProperties.builder()
     * .severity(RuleProperties.Severity.error)
     * .precision("high").build())));
     * assertEquals(ImmutableIssue.Severity.BLOCKER,
     * testee.mapRuleToIssueSeverity(Rule.Level.NONE,
     * Optional.of(RuleProperties.builder()
     * .severity(RuleProperties.Severity.error)
     * .precision("very-high").build())));
     * assertEquals(ImmutableIssue.Severity.CRITICAL,
     * testee.mapRuleToIssueSeverity(Rule.Level.NONE,
     * Optional.of(RuleProperties.builder()
     * .severity(RuleProperties.Severity.error)
     * .precision("<invalid>").build())));
     *
     * assertNull(testee.mapRuleToIssueSeverity(null,
     * Optional.of(RuleProperties.builder().build())));
     * }
     */
    @Test
    void testMapType() {
        final SonarMapper testee = new SonarMapper();
        assertNull(testee.mapType(null));
        assertEquals(Issue.Type.CODE_SMELL, testee.mapType(Issue.Severity.INFO));
        assertEquals(Issue.Type.CODE_SMELL, testee.mapType(Issue.Severity.MINOR));
        assertEquals(Issue.Type.CODE_SMELL, testee.mapType(Issue.Severity.MAJOR));
        assertEquals(Issue.Type.BUG, testee.mapType(Issue.Severity.BLOCKER));
        assertEquals(Issue.Type.VULNERABILITY, testee.mapType(Issue.Severity.CRITICAL));
    }

    /*
     * @Test
     * void testMapPrimaryLocation() {
     * final SonarIssueMapper testee = new SonarIssueMapper();
     * final Location primLoc = createTestLocation("uriPrimLoc", 80, 5, 6, 27);
     * final Location secondLoc = createTestLocation("uriSecondLoc", 81, 7, 20, 14);
     *
     * final ImmutableSonarLocation result =
     * testee.mapPrimaryLocation(ImmutableResult.builder()
     * .locations(Arrays.asList(primLoc, secondLoc))
     * .message("Test primary Location")
     * .build()).get();
     *
     * assertNotNull(result);
     * assertEquals("uriPrimLoc", result.filePath());
     * assertEquals("Test primary Location", result.message());
     * final ImmutableTextRange textRange = result.textRange();
     * assertNotNull(textRange);
     * assertEquals(27, textRange.startLine());
     * assertEquals(5, textRange.startColumn().get());
     * assertEquals(6, textRange.endColumn().get());
     *
     * assertEquals(testee.mapPrimaryLocation(Result.builder().build()),
     * Optional.empty());
     * assertEquals(testee.mapPrimaryLocation(
     * Result.builder().locations(Collections.emptyList()).build()),
     * Optional.empty());
     * }
     */
    /*
     * @Test
     * void testMapSecondaryLocations() {
     * final SonarIssueMapper testee = new SonarIssueMapper();
     *
     * final Location primLoc = createTestLocation("uriPrimLoc", 80, 5, 6, 27);
     * final Location secondLoc = createTestLocation("uriSecondLoc", 81, 7, 20, 14);
     * final Location duplicate = createTestLocation("uriSecondLoc", 81, 7, 20, 14);
     * final Location secondLoc2 = createTestLocation("thirdLocation", 90, 3, 9, 8);
     *
     * final Result input = Result.builder()
     * .locations(Arrays.asList(primLoc, secondLoc, duplicate, secondLoc2))
     * .message("Test secondary Locations")
     * .build();
     * final Set<ImmutableSonarLocation> results =
     * testee.mapSecondaryLocations(input).get();
     *
     * assertNotNull(results);
     * assertEquals(4, input.locations().get().size());
     * assertEquals(2, results.size(),
     * "Assumed primary location and duplicate are not included");
     * assertEquals(0, results.stream().filter(loc ->
     * loc.filePath().equals("uriPrimLoc")).count());
     * assertEquals(1, results.stream().filter(loc ->
     * loc.filePath().equals("uriSecondLoc")).count());
     * final ImmutableSonarLocation thirdLocation = results.stream()
     * .filter(loc ->
     * loc.filePath().equals("thirdLocation")).findFirst().orElse(null);
     * assertNotNull(thirdLocation);
     * assertEquals("Test secondary Locations", thirdLocation.message());
     * final ImmutableTextRange textRange = thirdLocation.textRange();
     * assertNotNull(textRange);
     * assertEquals(8, textRange.startLine());
     * assertEquals(3, textRange.startColumn().get());
     * assertEquals(9, textRange.endColumn().get());
     * }
     */
    private Rule createTestRule(RuleProperties properties) {
        return new Rule(Optional.of(TEST_RULE_ID),
                Optional.of("TestRuleName"), Optional.empty(), Optional.empty(),
                Optional.of(Rule.Level.ERROR),
                Optional.of(properties));
    }

    private RuleProperties createRuleProperties(Severity severity) {
        return new RuleProperties(Optional.of(TEST_RULE_ID),
                Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.of(severity));
    }

    private Result createTestResult(String uri) {
        return new Result(
                Optional.of(TEST_RULE_ID),
                Optional.of(4),
                Optional.of("TestMessage"),
                Optional.of(Collections.singletonList(createTestLocation(uri, 4, 1, 2, 3))));
    }

    private Location createTestLocation(String uri, int index, int startColumn, int endColumn,
            int startLine) {
        return new Location(uri,
                Optional.of(TEST_URI_BASE_ID),
                Optional.of(index),
                Optional.of(new Region(startLine, Optional.of(startColumn),
                        Optional.of(endColumn))));
    }
}
