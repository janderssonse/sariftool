package se.janderssonse.sarifconvert.cli.sonar;

import se.janderssonse.sarifconvert.cli.sarif.dto.*;
import se.janderssonse.sarifconvert.cli.sonar.dto.Issue;
import se.janderssonse.sarifconvert.cli.sonar.dto.Issues;
import se.janderssonse.sarifconvert.cli.sonar.dto.TextRange;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SonarIssueMapperTest {

  public static final String TEST_RULE_ID = "testRuleId";
  public static final String TEST_URI_BASE_ID = "testUriBaseId";

  @Test
  void testParserCallback_onFinding() {
    final SonarIssueMapper testee = new SonarIssueMapper();

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
    final SonarIssueMapper testee = new SonarIssueMapper();

    testee.onDriver(Driver.builder().name("driverName").organization("DriverOrg").semanticVersion("DriverVersion").build());
    testee.onRule(createTestRule());
    testee.onFinding(createTestResult("testUri"));

    assertEquals("parsed 1 Rules, 1 Results resulting in 1 issues.", testee.getSummary());
    assertEquals(1, testee.getMappedIssues(null).getResult().size());

    final Issue issue = testee.getMappedIssues(null).getResult().get(0);
    assertEquals(TEST_RULE_ID, issue.getRuleId());
    assertEquals(Issue.Severity.BLOCKER, issue.getSeverity());
    assertNotNull(issue.getPrimaryLocation());
    assertNull(issue.getSecondaryLocations());
    assertEquals("DriverOrg driverName vDriverVersion", issue.getEngineId());
  }

  @Test
  void testMappedIssues_FilterResults() {
    final SonarIssueMapper testee = new SonarIssueMapper();
    testee.onDriver(Driver.builder().name("driverName").organization("DriverOrg").semanticVersion("DriverVersion").build());
    testee.onRule(createTestRule());
    testee.onFinding(createTestResult("src/test/java/MyTestClass.java"));
    testee.onFinding(createTestResult("src/main/java/mypackage/MyClass.java"));

    assertEquals(2, testee.getMappedIssues(null).getResult().size());
    assertMatchingIssue(testee, new String[]{"/test/"}, "src/main/java/mypackage/MyClass.java");
    assertMatchingIssue(testee, new String[]{"/TEST/"}, "src/main/java/mypackage/MyClass.java");
    assertMatchingIssue(testee, new String[]{"mypackage"}, "src/test/java/MyTestClass.java");
    assertMatchingIssue(testee, new String[]{"mypackage"}, "src/test/java/MyTestClass.java");

    testee.onFinding(createTestResult("src/main/java/another/package/AnyTester.java"));
    assertEquals(3, testee.getMappedIssues(null).getResult().size());
    assertMatchingIssue(testee, new String[]{"mypackage","another/package"}, "src/test/java/MyTestClass.java");
    assertMatchingIssue(testee, new String[]{"(test)"}, "src/main/java/mypackage/MyClass.java");
    assertMatchingIssue(testee, new String[]{"(my[\\S]*\\.java)"}, "src/main/java/another/package/AnyTester.java");
  }

  private void assertMatchingIssue(SonarIssueMapper testee, String[] patternsToExclude, String expected1stPath) {
    final Issues mappedIssuesFiltered = testee.getMappedIssues(patternsToExclude);
    assertEquals(1, mappedIssuesFiltered.getResult().size());
    assertEquals(expected1stPath, mappedIssuesFiltered.getResult().get(0).getPrimaryLocation().getFilePath());
  }

  @Test
  void testParserCallback_onVersion() {
    final SonarIssueMapper testee = new SonarIssueMapper();
    assertNull(testee.getVersion());
    testee.onVersion("AnyVersion");
    assertEquals("AnyVersion", testee.getVersion());
  }

  @Test
  void testParserCallback_onSchema() {
    final SonarIssueMapper testee = new SonarIssueMapper();
    assertNull(testee.getSchema());
    testee.onSchema("AnySchema");
    assertEquals("AnySchema", testee.getSchema());
  }

  @Test
  void testParserCallback_onRule() {
    final SonarIssueMapper testee = new SonarIssueMapper();
    assertEquals("parsed 0 Rules, 0 Results resulting in 0 issues.", testee.getSummary());
    testee.onRule(createTestRule());
    assertEquals("parsed 1 Rules, 0 Results resulting in 0 issues.", testee.getSummary());
  }

  @Test
  void testMapSeverity_noMatch_ReturnInfo() {
    final SonarIssueMapper testee = new SonarIssueMapper();
    assertEquals(Issue.Severity.INFO, testee.mapSeverity(null));

    final Rule testRule = createTestRule();
    testRule.getProperties().setSeverity(null);
    testee.onRule(testRule);

    assertEquals(Issue.Severity.INFO, testee.mapSeverity(TEST_RULE_ID));
  }

  @Test
  void testMapRuleToIssueSeverity_LenientInput() {
    final SonarIssueMapper testee = new SonarIssueMapper();
    assertNull(testee.mapRuleToIssueSeverity(null, null));
    assertNull(testee.mapRuleToIssueSeverity(null, RuleProperties.builder().build()));
    assertEquals(Issue.Severity.MINOR, testee.mapRuleToIssueSeverity(Rule.Level.NONE, RuleProperties.builder().build()));
    assertEquals(Issue.Severity.MINOR, testee.mapRuleToIssueSeverity(Rule.Level.NOTE, RuleProperties.builder().build()));
    assertEquals(Issue.Severity.MAJOR, testee.mapRuleToIssueSeverity(Rule.Level.WARNING, RuleProperties.builder().build()));
    assertEquals(Issue.Severity.CRITICAL, testee.mapRuleToIssueSeverity(Rule.Level.ERROR, RuleProperties.builder().build()));
  }

  @Test
  void testMapRuleToIssueSeverity() {
    final SonarIssueMapper testee = new SonarIssueMapper();

    assertEquals(Issue.Severity.INFO, testee.mapRuleToIssueSeverity(Rule.Level.NONE
            , RuleProperties.builder().severity(RuleProperties.Severity.recommendation).build()));

    assertEquals(Issue.Severity.MINOR, testee.mapRuleToIssueSeverity(null
            , RuleProperties.builder().severity(RuleProperties.Severity.warning).build()));
    assertEquals(Issue.Severity.MINOR, testee.mapRuleToIssueSeverity(Rule.Level.NONE
            , RuleProperties.builder().severity(RuleProperties.Severity.warning).precision("medium").build()));
    assertEquals(Issue.Severity.MAJOR, testee.mapRuleToIssueSeverity(Rule.Level.NONE
            , RuleProperties.builder().severity(RuleProperties.Severity.warning).precision("high").build()));
    assertEquals(Issue.Severity.CRITICAL, testee.mapRuleToIssueSeverity(Rule.Level.NONE
            , RuleProperties.builder().severity(RuleProperties.Severity.warning).precision("very-high").build()));
    assertEquals(Issue.Severity.MINOR, testee.mapRuleToIssueSeverity(null
            , RuleProperties.builder().severity(RuleProperties.Severity.warning).precision("<invalid>").build()));

    assertEquals(Issue.Severity.CRITICAL, testee.mapRuleToIssueSeverity(null
            , RuleProperties.builder().severity(RuleProperties.Severity.error).build()));
    assertEquals(Issue.Severity.BLOCKER, testee.mapRuleToIssueSeverity(Rule.Level.ERROR
            , RuleProperties.builder().severity(RuleProperties.Severity.error).build()));
    assertEquals(Issue.Severity.CRITICAL, testee.mapRuleToIssueSeverity(Rule.Level.NONE
            , RuleProperties.builder().severity(RuleProperties.Severity.error).build()));
    assertEquals(Issue.Severity.CRITICAL, testee.mapRuleToIssueSeverity(Rule.Level.NONE
            , RuleProperties.builder().severity(RuleProperties.Severity.error).precision("medium").build()));
    assertEquals(Issue.Severity.CRITICAL, testee.mapRuleToIssueSeverity(Rule.Level.NONE
            , RuleProperties.builder().severity(RuleProperties.Severity.error).precision("high").build()));
    assertEquals(Issue.Severity.BLOCKER, testee.mapRuleToIssueSeverity(Rule.Level.NONE
            , RuleProperties.builder().severity(RuleProperties.Severity.error).precision("very-high").build()));
    assertEquals(Issue.Severity.CRITICAL, testee.mapRuleToIssueSeverity(Rule.Level.NONE
            , RuleProperties.builder().severity(RuleProperties.Severity.error).precision("<invalid>").build()));

    assertNull(testee.mapRuleToIssueSeverity(null, RuleProperties.builder().build()));
  }

  @Test
  void testMapType() {
    final SonarIssueMapper testee = new SonarIssueMapper();
    assertNull(testee.mapType(null));
    assertEquals(Issue.Type.CODE_SMELL, testee.mapType(Issue.Severity.INFO));
    assertEquals(Issue.Type.CODE_SMELL, testee.mapType(Issue.Severity.MINOR));
    assertEquals(Issue.Type.CODE_SMELL, testee.mapType(Issue.Severity.MAJOR));
    assertEquals(Issue.Type.BUG, testee.mapType(Issue.Severity.BLOCKER));
    assertEquals(Issue.Type.VULNERABILITY, testee.mapType(Issue.Severity.CRITICAL));
  }

  @Test
  void testMapPrimaryLocation() {
    final SonarIssueMapper testee = new SonarIssueMapper();
    final Location primLoc = createTestLocation("uriPrimLoc", 80, 5, 6, 27);
    final Location secondLoc = createTestLocation("uriSecondLoc", 81, 7, 20, 14);

    final se.janderssonse.sarifconvert.cli.sonar.dto.Location result = testee.mapPrimaryLocation(Result.builder()
            .locations(Arrays.asList(primLoc, secondLoc))
            .message("Test primary Location")
            .build());

    assertNotNull(result);
    assertEquals("uriPrimLoc", result.getFilePath());
    assertEquals("Test primary Location", result.getMessage());
    final TextRange textRange = result.getTextRange();
    assertNotNull(textRange);
    assertEquals(27, textRange.getStartLine());
    assertEquals(5, textRange.getStartColumn());
    assertEquals(6, textRange.getEndColumn());

    assertNull(testee.mapPrimaryLocation(Result.builder().locations(null).build()));
    assertNull(testee.mapPrimaryLocation(Result.builder().locations(Collections.emptyList()).build()));
  }

  @Test
  void testMapSecondaryLocations() {
    final SonarIssueMapper testee = new SonarIssueMapper();
    final Location primLoc = createTestLocation("uriPrimLoc", 80, 5, 6, 27);
    final Location secondLoc = createTestLocation("uriSecondLoc", 81, 7, 20, 14);
    final Location duplicate = createTestLocation("uriSecondLoc", 81, 7, 20, 14);
    final Location secondLoc2 = createTestLocation("thirdLocation", 90, 3, 9, 8);

    final Result input = Result.builder()
            .locations(Arrays.asList(primLoc, secondLoc, duplicate, secondLoc2))
            .message("Test secondary Locations")
            .build();
    final Set<se.janderssonse.sarifconvert.cli.sonar.dto.Location> results = testee.mapSecondaryLocations(input);

    assertNotNull(results);
    assertEquals(4, input.getLocations().size());
    assertEquals(2, results.size(), "Assumed primary location and duplicate are not included");
    assertEquals(0, results.stream().filter(loc -> loc.getFilePath().equals("uriPrimLoc")).count());
    assertEquals(1, results.stream().filter(loc -> loc.getFilePath().equals("uriSecondLoc")).count());
    final se.janderssonse.sarifconvert.cli.sonar.dto.Location thirdLocation = results.stream()
            .filter(loc -> loc.getFilePath().equals("thirdLocation")).findFirst().orElse(null);
    assertNotNull(thirdLocation);
    assertEquals("Test secondary Locations", thirdLocation.getMessage());
    final TextRange textRange = thirdLocation.getTextRange();
    assertNotNull(textRange);
    assertEquals(8, textRange.getStartLine());
    assertEquals(3, textRange.getStartColumn());
    assertEquals(9, textRange.getEndColumn());
  }

  private Rule createTestRule() {
    return Rule.builder()
            .id(TEST_RULE_ID)
            .level(Rule.Level.ERROR)
            .name("TestRuleName")
            .properties(RuleProperties.builder()
                    .severity(RuleProperties.Severity.error)
                    .id(TEST_RULE_ID)
                    .build())
            .build();
  }

  private Result createTestResult(String uri) {
    return Result.builder()
            .message("TestMessage")
            .ruleId(TEST_RULE_ID)
            .ruleIndex(4)
            .locations(Collections.singletonList(createTestLocation(uri, 4, 1, 2, 3)))
            .build();
  }

  private Location createTestLocation(String uri, int index, int startColumn, int endColumn, int startLine) {
    return Location.builder()
            .uri(uri)
            .uriBaseId(TEST_URI_BASE_ID)
            .index(index)
            .region(Region.builder().startColumn(startColumn).endColumn(endColumn).startLine(startLine).build())
            .build();
  }
}
