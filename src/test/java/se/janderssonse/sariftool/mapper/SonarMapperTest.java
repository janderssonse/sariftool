// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sariftool.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import nl.altindag.log.LogCaptor;
import se.janderssonse.sariftool.SarifParser;
import se.janderssonse.sariftool.util.Util;

class SonarMapperTest {
    @TempDir
    File tmpDir;
    LogCaptor logCaptor = LogCaptor.forClass(SonarMapper.class);
    public static final String TEST_RULE_ID = "testRuleId";
    public static final String TEST_URI_BASE_ID = "testUriBaseId";

    @Test
    void when_invalid_sariffile_mapped_no_results_exists() throws URISyntaxException {

        final Path sarifFile = Paths.get(ClassLoader.getSystemResource("invalid.sarif").toURI());
        SarifParser parser = new SarifParser(sarifFile);
        SonarMapper mapper = new SonarMapper(parser);

        mapper.map(targetPath(sarifFile), List.of());

        assertTrue(logCaptor.getInfoLogs()
                .contains("Wrote file: target '" + targetPath(sarifFile) + "', issues '0', excluded paths: ''"));
        assertEquals(0, parser.getResults().size());
        assertEquals(0, parser.getRules().size());
        assertTrue(parser.getVersion().isEmpty());
        assertTrue(parser.getSchema().isEmpty());
    }

    @Test
    void when_valid_sariffile_it_is_mapped_correctly() throws URISyntaxException {

        final Path sarifFile = Paths.get(ClassLoader.getSystemResource("exampleWithTestDir.sarif").toURI());
        SarifParser parser = new SarifParser(sarifFile);
        SonarMapper mapper = new SonarMapper(parser);

        mapper.map(targetPath(sarifFile), List.of());

        assertTrue(logCaptor.getInfoLogs()
                .contains("Wrote file: target '" + targetPath(sarifFile) + "', issues '2', excluded paths: ''"));
        assertEquals(2, parser.getResults().size());
        assertEquals(10, parser.getRules().size());
        assertTrue(parser.getVersion().isPresent());
        assertTrue(parser.getSchema().isPresent());
    }

    @Test
    void when_valid_sariffile_it_is_mapped_to_sonar_format() throws URISyntaxException, IOException {

        final Path sarifFile = Paths.get(ClassLoader.getSystemResource("sonarmapperinput.sarif").toURI());
        SarifParser parser = new SarifParser(sarifFile);
        SonarMapper mapper = new SonarMapper(parser);

        mapper.map(targetPath(sarifFile), List.of());

        assertTrue(logCaptor.getInfoLogs()
                .contains("Wrote file: target '" + targetPath(sarifFile) + "', issues '1', excluded paths: ''"));
        assertEquals(1, parser.getResults().size());
        assertEquals(1, parser.getRules().size());

        final Path expectedSonarJson = Paths.get(ClassLoader.getSystemResource("sonarmapperoutput.json").toURI());
        String eString = Files.readString(expectedSonarJson);
        String tmpF = Files.readString(targetPath(sarifFile));

        assertEquals(eString.replaceAll("\\s+", ""), tmpF.replaceAll("\\s+", ""));
    }

    @Test
    void when_valid_sariffile_with_filter_it_is_mapped_correctly() throws URISyntaxException {

        final Path sarifFile = Paths.get(ClassLoader.getSystemResource("exampleWithTestDir.sarif").toURI());
        SarifParser parser = new SarifParser(sarifFile);
        SonarMapper mapper = new SonarMapper(parser);

        mapper.map(targetPath(sarifFile), List.of("src/test"));

        assertTrue(logCaptor.getInfoLogs()
                .contains(
                        "Wrote file: target '" + targetPath(sarifFile) + "', issues '1', excluded paths: 'src/test'"));
        assertEquals(2, parser.getResults().size());
        assertEquals(10, parser.getRules().size());
    }

    @Test
    void when_valid_sariffile_with_filter_for_all_it_is_mapped_correctly() throws URISyntaxException {

        final Path sarifFile = Paths.get(ClassLoader.getSystemResource("exampleWithTestDir.sarif").toURI());
        SarifParser parser = new SarifParser(sarifFile);
        SonarMapper mapper = new SonarMapper(parser);

        mapper.map(targetPath(sarifFile), List.of("src/test","src/main"));

        assertTrue(logCaptor.getInfoLogs()
                .contains(
                        "Wrote file: target '" + targetPath(sarifFile) + "', issues '0', excluded paths: 'src/test,src/main'"));
        assertEquals(2, parser.getResults().size());
        assertEquals(10, parser.getRules().size());
    }

    private Path targetPath(final Path sarifFile) {
        return Paths.get(tmpDir.toString(), Util.removeFileExtension(sarifFile, true).concat(".json"));
    }



/*   @Test
*   void testMapSeverity_noMatch_ReturnInfo() {
*   final SonarMapper testee = new SonarMapper();
*   assertEquals(Issue.Severity.INFO, testee.mapSeverity(null));

*   RuleProperties props = new RuleProperties(
*   Optional.of(TEST_RULE_ID), Optional.empty(),
*   Optional.empty(),
*   Optional.empty(),
*   Optional.empty(),
*   Optional.empty(),
*   Optional.empty());
*   final Rule testRule = createTestRule(props);

*   testee.onRule(testRule);

*   assertEquals(Issue.Severity.INFO, testee.mapSeverity(TEST_RULE_ID));
*/
//   }

    /*  @Test
     * void testMapRuleToIssueSeverity_LenientInput() {
     * final SonarMapper testee = new SonarMapper();
     * Optional<RuleProperties> rp = Optional.of(new
     * RuleProperties(Optional.empty(), Optional.empty(),
     * Optional.empty(), Optional.empty(), Optional.empty(),
     * Optional.empty(), Optional.empty()));
     * assertNull(testee.mapRuleToIssueSeverity(null, Optional.empty()));
     * assertNull(testee.mapRuleToIssueSeverity(null, rp));
     * assertEquals(Issue.Severity.MINOR,
     * testee.mapRuleToIssueSeverity(Rule.Level.NONE,
     * rp));
     * assertEquals(Issue.Severity.MINOR,
     * testee.mapRuleToIssueSeverity(Rule.Level.NOTE,
     * rp));
     * assertEquals(Issue.Severity.MAJOR,
     * testee.mapRuleToIssueSeverity(Rule.Level.WARNING,
     * rp));
     * assertEquals(Issue.Severity.CRITICAL,
     * testee.mapRuleToIssueSeverity(Rule.Level.ERROR,
     * rp));
     * }
     */
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
    /*
     * @Test
     * void testMapType() {
     * final SonarMapper testee = new SonarMapper();
     * assertNull(testee.mapType(null));
     * assertEquals(Issue.Type.CODE_SMELL, testee.mapType(Issue.Severity.INFO));
     * assertEquals(Issue.Type.CODE_SMELL, testee.mapType(Issue.Severity.MINOR));
     * assertEquals(Issue.Type.CODE_SMELL, testee.mapType(Issue.Severity.MAJOR));
     * assertEquals(Issue.Type.BUG, testee.mapType(Issue.Severity.BLOCKER));
     * assertEquals(Issue.Type.VULNERABILITY,
     * testee.mapType(Issue.Severity.CRITICAL));
     * }
     */
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

/*     private Rule createTestRule(RuleProperties properties) {
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
                Optional.of(Collections.singletonList(createTestLocation(uri, 4, 1, 2, 3, 26))));
    }
*/
 /*    private Location createTestLocation(String uri, int index, int startColumn,
            int endColumn,
            int startLine, int endLine) {
        return new Location(uri,
                Optional.of(TEST_URI_BASE_ID),
                Optional.of(index),
                Optional.of(new Region(startLine, Optional.of(endLine), Optional.of(startColumn),
                        Optional.of(endColumn))));
    }*/
}
