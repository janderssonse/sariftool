// SPDX-FileCopyrightText: 2021 Baloise Group
// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sarifconvert.cli.sonar;

import se.janderssonse.sarifconvert.cli.sarif.dto.ImmutableDriver;
import se.janderssonse.sarifconvert.cli.sarif.dto.ImmutableLocation;
import se.janderssonse.sarifconvert.cli.sarif.dto.ImmutableRegion;
import se.janderssonse.sarifconvert.cli.sarif.dto.ImmutableResult;
import se.janderssonse.sarifconvert.cli.sonar.dto.ImmutableIssue;
import se.janderssonse.sarifconvert.cli.sarif.dto.ImmutableRule;
import se.janderssonse.sarifconvert.cli.sarif.dto.ImmutableRuleProperties;
import se.janderssonse.sarifconvert.cli.sarif.dto.RuleProperties.Severity;
import se.janderssonse.sarifconvert.cli.sonar.dto.ImmutableSonarLocation;
import se.janderssonse.sarifconvert.cli.sonar.dto.ImmutableTextRange;
import se.janderssonse.sarifconvert.cli.sonar.dto.Issues;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SonarIssueMapperTest {

        public static final String TEST_RULE_ID = "testRuleId";
        public static final String TEST_URI_BASE_ID = "testUriBaseId";

        @Test
        void testParserCallback_onFinding() {
                final SonarIssueMapper testee =new SonarIssueMapper();

                assertEquals("parsed 0 Rules, 0 Results resulting in 0 issues.", testee.getSummary());
                assertEquals(0, testee.getMappedIssues(null).getResult().size());
                assertEquals(0, testee.getMappedIssues(new String[0]).getResult().size());

                testee.onFinding(null);
                assertEquals("parsed 0 Rules, 0 Results resulting in 0 issues.", testee.getSummary());
                assertEquals(0, testee.getMappedIssues(null).getResult().size());
                assertEquals(0, testee.getMappedIssues(new String[0]).getResult().size());

                testee.onFinding(createTestResult("testUri"));

                assertEquals("parsed 0 Rules, 1 Results resulting in 1 issues.", testee.getSummary());
                assertEquals(1, testee.getMappedIssues(null).getResult().size());
                assertEquals(1, testee.getMappedIssues(new String[0]).getResult().size());
        }

        @Test
        void testMapperInjected() {
                ImmutableRuleProperties properties = createRuleProperties(Severity.error);
                final SonarIssueMapper testee = new SonarIssueMapper();
                                testee.setDriver(ImmutableDriver.builder().name("driverName")
                                                .organization("DriverOrg")
                                                .semanticVersion("DriverVersion")
                                                .build());
                                testee.setRules(List.of(createTestRule(properties)));

                testee.onFinding(createTestResult("testUri"));
                assertEquals("parsed 1 Rules, 1 Results resulting in 1 issues.", testee.getSummary());
                assertEquals(1, testee.getMappedIssues(null).getResult().size());

                final ImmutableIssue issue = testee.getMappedIssues(null).getResult().get(0);
                assertEquals(TEST_RULE_ID, issue.ruleId().get());
                assertEquals(ImmutableIssue.Severity.BLOCKER, issue.severity().get());
                assertNotNull(issue.primaryLocation().get());
                assertEquals(issue.secondaryLocations(), Optional.empty());
                assertEquals("DriverOrg driverName vDriverVersion", issue.engineId().get());
        }

        @Test
        void testMappedIssues_FilterResults() {
                ImmutableRuleProperties properties = createRuleProperties(Severity.error);
                final SonarIssueMapper testee = new SonarIssueMapper();
                testee.setDriver(ImmutableDriver.builder().name("driverName").organization("DriverOrg")
                                                .semanticVersion("DriverVersion").build());
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

        private void assertMatchingIssue(SonarIssueMapper testee, String[] patternsToExclude, String expected1stPath) {
                final Issues mappedIssuesFiltered = testee.getMappedIssues(patternsToExclude);
                assertEquals(1, mappedIssuesFiltered.getResult().size());
                assertEquals(expected1stPath, mappedIssuesFiltered.getResult().get(0).primaryLocation().get().filePath());
        }

        @Test
        void testParserCallback_onVersion() {
                final SonarIssueMapper testee =new SonarIssueMapper();
                assertNull(testee.getVersion());
                testee.onVersion("AnyVersion");
                assertEquals("AnyVersion", testee.getVersion());
        }

        @Test
        void testParserCallback_onSchema() {
                final SonarIssueMapper testee =new SonarIssueMapper();
                assertNull(testee.getSchema());
                testee.onSchema("AnySchema");
                assertEquals("AnySchema", testee.getSchema());
        }

        @Test
        void testParserCallback_onRule() {
                final SonarIssueMapper testee =new SonarIssueMapper();
                assertEquals("parsed 0 Rules, 0 Results resulting in 0 issues.", testee.getSummary());
                ImmutableRuleProperties properties = createRuleProperties(Severity.error);
                testee.onRule(createTestRule(properties));
                assertEquals("parsed 1 Rules, 0 Results resulting in 0 issues.", testee.getSummary());
        }

        @Test
        void testMapSeverity_noMatch_ReturnInfo() {
                final SonarIssueMapper testee =new SonarIssueMapper();
                assertEquals(ImmutableIssue.Severity.INFO, testee.mapSeverity(null));

                ImmutableRuleProperties props = ImmutableRuleProperties.builder()
                                .id(TEST_RULE_ID)
                                .build();
                final ImmutableRule testRule = createTestRule(props);

                testee.onRule(testRule);

                assertEquals(ImmutableIssue.Severity.INFO, testee.mapSeverity(TEST_RULE_ID));
        }

        @Test
        void testMapRuleToIssueSeverity_LenientInput() {
                final SonarIssueMapper testee =new SonarIssueMapper();
                assertNull(testee.mapRuleToIssueSeverity(null, Optional.empty()));
                assertNull(testee.mapRuleToIssueSeverity(null, Optional.of(ImmutableRuleProperties.builder().build())));
                assertEquals(ImmutableIssue.Severity.MINOR, testee.mapRuleToIssueSeverity(ImmutableRule.Level.NONE,
                                Optional.of(ImmutableRuleProperties.builder().build())));
                assertEquals(ImmutableIssue.Severity.MINOR, testee.mapRuleToIssueSeverity(ImmutableRule.Level.NOTE,
                                Optional.of(ImmutableRuleProperties.builder().build())));
                assertEquals(ImmutableIssue.Severity.MAJOR, testee.mapRuleToIssueSeverity(ImmutableRule.Level.WARNING,
                                Optional.of(ImmutableRuleProperties.builder().build())));
                assertEquals(ImmutableIssue.Severity.CRITICAL, testee.mapRuleToIssueSeverity(ImmutableRule.Level.ERROR,
                                Optional.of(ImmutableRuleProperties.builder().build())));
        }

        @Test
        void testMapRuleToIssueSeverity() {
                final SonarIssueMapper testee =new SonarIssueMapper();

                assertEquals(ImmutableIssue.Severity.INFO, testee.mapRuleToIssueSeverity(ImmutableRule.Level.NONE,
                                Optional.of(ImmutableRuleProperties.builder()
                                                .severity(ImmutableRuleProperties.Severity.recommendation).build())));

                assertEquals(ImmutableIssue.Severity.MINOR, testee.mapRuleToIssueSeverity(null,
                                Optional.of(ImmutableRuleProperties.builder()
                                                .severity(ImmutableRuleProperties.Severity.warning).build())));
                assertEquals(ImmutableIssue.Severity.MINOR,
                                testee.mapRuleToIssueSeverity(ImmutableRule.Level.NONE,
                                                Optional.of(ImmutableRuleProperties.builder()
                                                                .severity(ImmutableRuleProperties.Severity.warning)
                                                                .precision("medium").build())));
                assertEquals(ImmutableIssue.Severity.MAJOR,
                                testee.mapRuleToIssueSeverity(ImmutableRule.Level.NONE,
                                                Optional.of(ImmutableRuleProperties.builder()
                                                                .severity(ImmutableRuleProperties.Severity.warning)
                                                                .precision("high").build())));
                assertEquals(ImmutableIssue.Severity.CRITICAL,
                                testee.mapRuleToIssueSeverity(ImmutableRule.Level.NONE,
                                                Optional.of(ImmutableRuleProperties.builder()
                                                                .severity(ImmutableRuleProperties.Severity.warning)
                                                                .precision("very-high").build())));
                assertEquals(ImmutableIssue.Severity.MINOR, testee.mapRuleToIssueSeverity(null,
                                Optional.of(ImmutableRuleProperties.builder()
                                                .severity(ImmutableRuleProperties.Severity.warning)
                                                .precision("<invalid>").build())));

                assertEquals(ImmutableIssue.Severity.CRITICAL, testee.mapRuleToIssueSeverity(null,
                                Optional.of(ImmutableRuleProperties
                                                .builder().severity(ImmutableRuleProperties.Severity.error).build())));
                assertEquals(ImmutableIssue.Severity.BLOCKER,
                                testee.mapRuleToIssueSeverity(ImmutableRule.Level.ERROR,
                                                Optional.of(ImmutableRuleProperties
                                                                .builder()
                                                                .severity(ImmutableRuleProperties.Severity.error)
                                                                .build())));
                assertEquals(ImmutableIssue.Severity.CRITICAL,
                                testee.mapRuleToIssueSeverity(ImmutableRule.Level.NONE,
                                                Optional.of(ImmutableRuleProperties
                                                                .builder()
                                                                .severity(ImmutableRuleProperties.Severity.error)
                                                                .build())));
                assertEquals(ImmutableIssue.Severity.CRITICAL,
                                testee.mapRuleToIssueSeverity(ImmutableRule.Level.NONE,
                                                Optional.of(ImmutableRuleProperties.builder()
                                                                .severity(ImmutableRuleProperties.Severity.error)
                                                                .precision("medium").build())));
                assertEquals(ImmutableIssue.Severity.CRITICAL,
                                testee.mapRuleToIssueSeverity(ImmutableRule.Level.NONE,
                                                Optional.of(ImmutableRuleProperties.builder()
                                                                .severity(ImmutableRuleProperties.Severity.error)
                                                                .precision("high").build())));
                assertEquals(ImmutableIssue.Severity.BLOCKER,
                                testee.mapRuleToIssueSeverity(ImmutableRule.Level.NONE,
                                                Optional.of(ImmutableRuleProperties.builder()
                                                                .severity(ImmutableRuleProperties.Severity.error)
                                                                .precision("very-high").build())));
                assertEquals(ImmutableIssue.Severity.CRITICAL,
                                testee.mapRuleToIssueSeverity(ImmutableRule.Level.NONE,
                                                Optional.of(ImmutableRuleProperties.builder()
                                                                .severity(ImmutableRuleProperties.Severity.error)
                                                                .precision("<invalid>").build())));

                assertNull(testee.mapRuleToIssueSeverity(null, Optional.of(ImmutableRuleProperties.builder().build())));
        }

        @Test
        void testMapType() {
                final SonarIssueMapper testee =new SonarIssueMapper();
                assertNull(testee.mapType(null));
                assertEquals(ImmutableIssue.Type.CODE_SMELL, testee.mapType(ImmutableIssue.Severity.INFO));
                assertEquals(ImmutableIssue.Type.CODE_SMELL, testee.mapType(ImmutableIssue.Severity.MINOR));
                assertEquals(ImmutableIssue.Type.CODE_SMELL, testee.mapType(ImmutableIssue.Severity.MAJOR));
                assertEquals(ImmutableIssue.Type.BUG, testee.mapType(ImmutableIssue.Severity.BLOCKER));
                assertEquals(ImmutableIssue.Type.VULNERABILITY, testee.mapType(ImmutableIssue.Severity.CRITICAL));
        }

        @Test
        void testMapPrimaryLocation() {
                final SonarIssueMapper testee =new SonarIssueMapper();
                final ImmutableLocation primLoc = createTestLocation("uriPrimLoc", 80, 5, 6, 27);
                final ImmutableLocation secondLoc = createTestLocation("uriSecondLoc", 81, 7, 20, 14);

                final ImmutableSonarLocation result = testee.mapPrimaryLocation(ImmutableResult.builder()
                                .locations(Arrays.asList(primLoc, secondLoc))
                                .message("Test primary Location")
                                .build()).get();

                assertNotNull(result);
                assertEquals("uriPrimLoc", result.filePath());
                assertEquals("Test primary Location", result.message());
                final ImmutableTextRange textRange = result.textRange();
                assertNotNull(textRange);
                assertEquals(27, textRange.startLine());
                assertEquals(5, textRange.startColumn().get());
                assertEquals(6, textRange.endColumn().get());

                assertEquals(testee.mapPrimaryLocation(ImmutableResult.builder().build()),Optional.empty());
                assertEquals(testee.mapPrimaryLocation(
                                ImmutableResult.builder().locations(Collections.emptyList()).build()),Optional.empty());
        }

        @Test
        void testMapSecondaryLocations() {
                final SonarIssueMapper testee =new SonarIssueMapper();

                final ImmutableLocation primLoc = createTestLocation("uriPrimLoc", 80, 5, 6, 27);
                final ImmutableLocation secondLoc = createTestLocation("uriSecondLoc", 81, 7, 20, 14);
                final ImmutableLocation duplicate = createTestLocation("uriSecondLoc", 81, 7, 20, 14);
                final ImmutableLocation secondLoc2 = createTestLocation("thirdLocation", 90, 3, 9, 8);

                final ImmutableResult input = ImmutableResult.builder()
                                .locations(Arrays.asList(primLoc, secondLoc, duplicate, secondLoc2))
                                .message("Test secondary Locations")
                                .build();
                final Set<ImmutableSonarLocation> results = testee.mapSecondaryLocations(input).get();

                assertNotNull(results);
                assertEquals(4, input.locations().get().size());
                assertEquals(2, results.size(), "Assumed primary location and duplicate are not included");
                assertEquals(0, results.stream().filter(loc -> loc.filePath().equals("uriPrimLoc")).count());
                assertEquals(1, results.stream().filter(loc -> loc.filePath().equals("uriSecondLoc")).count());
                final ImmutableSonarLocation thirdLocation = results.stream()
                                .filter(loc -> loc.filePath().equals("thirdLocation")).findFirst().orElse(null);
                assertNotNull(thirdLocation);
                assertEquals("Test secondary Locations", thirdLocation.message());
                final ImmutableTextRange textRange = thirdLocation.textRange();
                assertNotNull(textRange);
                assertEquals(8, textRange.startLine());
                assertEquals(3, textRange.startColumn().get());
                assertEquals(9, textRange.endColumn().get());
        }

        private ImmutableRule createTestRule(ImmutableRuleProperties properties) {
                return ImmutableRule.builder()
                                .id(TEST_RULE_ID)
                                .level(ImmutableRule.Level.ERROR)
                                .name("TestRuleName")
                                .properties(properties)
                                .build();
        }

        private ImmutableRuleProperties createRuleProperties(Severity severity) {
                return ImmutableRuleProperties.builder()
                                .severity(severity)
                                .id(TEST_RULE_ID)
                                .build();
        }

        private ImmutableResult createTestResult(String uri) {
                return ImmutableResult.builder()
                                .message("TestMessage")
                                .ruleId(TEST_RULE_ID)
                                .ruleIndex(4)
                                .locations(Collections.singletonList(createTestLocation(uri, 4, 1, 2, 3)))
                                .build();
        }

        private ImmutableLocation createTestLocation(String uri, int index, int startColumn, int endColumn,
                        int startLine) {
                return ImmutableLocation.builder()
                                .uri(uri)
                                .uriBaseId(TEST_URI_BASE_ID)
                                .index(index)
                                .region(ImmutableRegion.builder().startColumn(startColumn).endColumn(endColumn)
                                                .startLine(startLine).build())
                                .build();
        }
}
