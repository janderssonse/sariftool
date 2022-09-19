// SPDX-FileCopyrightText: 2021 Baloise Group
// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sarifconvert.cli.sarif;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import se.janderssonse.sarifconvert.cli.sarif.dto.ImmutableDriver;
import se.janderssonse.sarifconvert.cli.sarif.dto.ImmutableResult;
import se.janderssonse.sarifconvert.cli.sarif.dto.ImmutableRule;
import se.janderssonse.sarifconvert.cli.sarif.dto.ImmutableRuleProperties;
import se.janderssonse.sarifconvert.cli.sarif.dto.Location;
import se.janderssonse.sarifconvert.cli.sarif.dto.Region;
import se.janderssonse.sarifconvert.cli.sarif.dto.Rule;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SarifParserTest {

  @Captor
  ArgumentCaptor<String> versionCaptor;

  @Captor
  ArgumentCaptor<String> schemaCaptor;

  @Captor
  ArgumentCaptor<ImmutableDriver> driverCaptor;

  @Captor
  ArgumentCaptor<ImmutableRule> ruleCaptor;

  @Captor
  ArgumentCaptor<ImmutableResult> resultCaptor;

  @Test
  @DisplayName("Non SARIF file does not invoke any callback function")
  void execute_NonSarifFile_CallbackNotInvoked() throws URISyntaxException, IOException {
    final ParserCallback mockedParserCB = Mockito.mock(ParserCallback.class);
    final File exampleSarifFile = new File(ClassLoader.getSystemResource("anyOther.json").toURI());

    final Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      SarifParser.execute(exampleSarifFile, mockedParserCB);
    });
    assertTrue(exception.getMessage().startsWith("Validation failed:"));

    assertAll(
        () -> Mockito.verify(mockedParserCB, Mockito.never()).onFinding(ArgumentMatchers.any(ImmutableResult.class)),
        () -> Mockito.verify(mockedParserCB, Mockito.never()).onVersion(ArgumentMatchers.anyString()),
        () -> Mockito.verify(mockedParserCB, Mockito.never()).onSchema(ArgumentMatchers.anyString()),
        () -> Mockito.verify(mockedParserCB, Mockito.never()).onDriver(ArgumentMatchers.any(ImmutableDriver.class)),
        () -> Mockito.verify(mockedParserCB, Mockito.never()).onRule(ArgumentMatchers.any(ImmutableRule.class)));
  }

  @Test
  @DisplayName("Happy Case parse example.sarif")
  void execute_testFile_HappyCase() throws URISyntaxException, IOException {
    final ParserCallback mockedParserCB = Mockito.mock(ParserCallback.class);
    final File exampleSarifFile = new File(ClassLoader.getSystemResource("example.sarif").toURI());

    SarifParser.execute(exampleSarifFile, mockedParserCB);

    Mockito.verify(mockedParserCB, Mockito.times(1)).onVersion(versionCaptor.capture());
    assertEquals("2.1.0", versionCaptor.getValue());

    Mockito.verify(mockedParserCB, Mockito.times(1)).onSchema(schemaCaptor.capture());
    assertEquals("https://raw.githubusercontent.com/oasis-tcs/sarif-spec/master/Schemata/sarif-schema-2.1.0.json",
        schemaCaptor.getValue());

    Mockito.verify(mockedParserCB, Mockito.times(1)).onDriver(driverCaptor.capture());
    verifyDriver(driverCaptor.getValue(), "2.3.3");

    Mockito.verify(mockedParserCB, Mockito.times(10)).onRule(ruleCaptor.capture());
    final List<ImmutableRule> rulesCaptured = ruleCaptor.getAllValues();
    verifyRule_EmptySynchBlock(rulesCaptured.get(2));
    verifyRule_impossibleArrayCast(rulesCaptured.get(3));

    Mockito.verify(mockedParserCB, Mockito.times(1)).onFinding(resultCaptor.capture());
    verifyResult(resultCaptor.getValue());
  }

  private void verifyResult(ImmutableResult result) {
    assertEquals("java/misleading-indentation", result.ruleId().get());
    assertEquals(9, result.ruleIndex().get());
    assertEquals(
        "Indentation suggests that [the next statement](1) belongs to [the control structure](2), but this is not the case; consider adding braces or adjusting indentation.",
        result.message().get());
    assertNotNull(result.locations());
    assertEquals(1, result.locations().get().size());
    final Location location = result.locations().get().get(0);
    assertEquals("src/main/java/org/arburk/fishbone/infrastructure/service/FishRepository.java", location.uri());
    assertEquals("%SRCROOT%", location.uriBaseId().get());
    assertEquals(0, location.index().get());
    assertNotNull(location.region());
    final Region region = location.region().get();
    assertEquals(26, region.startLine());
    assertEquals(9, region.startColumn().get());
    assertEquals(13, region.endColumn().get());
  }

  private void verifyRule_impossibleArrayCast(Rule impossibleArrayCast) {
    assertEquals("java/impossible-array-cast", impossibleArrayCast.id().get());
    assertEquals("java/impossible-array-cast", impossibleArrayCast.name().get());
    assertEquals("Impossible array cast", impossibleArrayCast.shortDescription().get());
    assertEquals(
        "Trying to cast an array of a particular type as an array of a subtype causes a 'ClassCastException' at runtime.",
        impossibleArrayCast.fullDescription().get());
    assertNotNull(impossibleArrayCast.level());
    assertEquals(ImmutableRule.Level.ERROR, impossibleArrayCast.level().get());
    assertNotNull(impossibleArrayCast.properties());
    final ImmutableRuleProperties icProperties = impossibleArrayCast.properties().get();
    assertEquals("java/impossible-array-cast", icProperties.id().get());
    assertEquals("Impossible array cast", icProperties.name().get());
    assertEquals(
        "Trying to cast an array of a particular type as an array of a subtype causes a\n              'ClassCastException' at runtime.",
        icProperties.description().get());
    assertEquals("low", icProperties.precision().get());
    assertEquals("problem", icProperties.kind().get());
    assertEquals(ImmutableRuleProperties.Severity.error, icProperties.severity().get());
    final ArrayList<String> icTags = icProperties.tags().get();
    assertEquals(4, icTags.size());
    assertEquals(1, icTags.stream().filter("reliability"::equals).count());
    assertEquals(1, icTags.stream().filter("correctness"::equals).count());
    assertEquals(1, icTags.stream().filter("logic"::equals).count());
    assertEquals(1, icTags.stream().filter("external/cwe/cwe-704"::equals).count());
  }

  private void verifyRule_EmptySynchBlock(ImmutableRule emptySynchBlock) {
    assertEquals("java/empty-synchronized-block", emptySynchBlock.id().get());
    assertEquals("java/empty-synchronized-block", emptySynchBlock.name().get());
    assertEquals("Empty synchronized block", emptySynchBlock.shortDescription().get());
    assertEquals(
        "Empty synchronized blocks may indicate the presence of incomplete code or incorrect synchronization, and may lead to concurrency problems.",
        emptySynchBlock.fullDescription().get());
    assertEquals(emptySynchBlock.level(), Optional.empty());
    assertNotNull(emptySynchBlock.properties().get());
    final ImmutableRuleProperties esbProperties = emptySynchBlock.properties().get();
    assertEquals("java/empty-synchronized-block", esbProperties.id().get());
    assertEquals("Empty synchronized block", esbProperties.name().get());
    assertEquals(
        "Empty synchronized blocks may indicate the presence of\n              incomplete code or incorrect synchronization, and may lead to concurrency problems.",
        esbProperties.description().get());
    assertEquals("low", esbProperties.precision().get());
    assertEquals("problem", esbProperties.kind().get());
    assertEquals(ImmutableRuleProperties.Severity.warning, esbProperties.severity().get());
    final ArrayList<String> esbTags = esbProperties.tags().get();
    assertEquals(5, esbTags.size());
    assertEquals(1, esbTags.stream().filter("reliability"::equals).count());
    assertEquals(1, esbTags.stream().filter("correctness"::equals).count());
    assertEquals(1, esbTags.stream().filter("concurrency"::equals).count());
    assertEquals(1, esbTags.stream().filter("language-features"::equals).count());
    assertEquals(1, esbTags.stream().filter("external/cwe/cwe-585"::equals).count());
  }

  private void verifyDriver(ImmutableDriver driverCaptured, String versionExpected) {
    assertEquals("GitHub", driverCaptured.organization().get());
    assertEquals("CodeQL", driverCaptured.name().get());
    assertEquals(versionExpected, driverCaptured.semanticVersion().get());
  }

  @Test
  @DisplayName("Check FileNotFoundException on inexistent input file")
  void execute_wrongFile_FNFException() {
    assertThrows(FileNotFoundException.class, () -> SarifParser.execute(new File(""), (ParserCallback) null));
  }

  @Test
  @DisplayName("Verify extension rules are properly parsed using rulesInExtensions.sarif")
  void execute_rulesInExtensionsTest() throws URISyntaxException, IOException {
    final ParserCallback mockedParserCB = Mockito.mock(ParserCallback.class);
    final File exampleSarifFile = new File(ClassLoader.getSystemResource("rulesInExtensions.sarif").toURI());

    SarifParser.execute(exampleSarifFile, mockedParserCB);

    Mockito.verify(mockedParserCB, Mockito.times(1)).onVersion(versionCaptor.capture());
    assertEquals("2.1.0", versionCaptor.getValue());

    Mockito.verify(mockedParserCB, Mockito.times(1)).onSchema(schemaCaptor.capture());
    assertEquals("https://raw.githubusercontent.com/oasis-tcs/sarif-spec/master/Schemata/sarif-schema-2.1.0.json",
        schemaCaptor.getValue());

    Mockito.verify(mockedParserCB, Mockito.times(1)).onDriver(driverCaptor.capture());
    verifyDriver(driverCaptor.getValue(), "2.5.5");

    Mockito.verify(mockedParserCB, Mockito.times(166)).onRule(ruleCaptor.capture());
    final List<ImmutableRule> rulesCaptured = ruleCaptor.getAllValues();

    assertEquals(166, rulesCaptured.size());

    final ImmutableRule sqlInjection = rulesCaptured.get(0);
    assertEquals("java/sql-injection", sqlInjection.id().get());
    assertEquals("java/sql-injection", sqlInjection.name().get());
    assertEquals("Query built from user-controlled sources", sqlInjection.shortDescription().get());
    assertEquals(
        "Building a SQL or Java Persistence query from user-controlled sources is vulnerable to insertion of malicious code by the user.",
        sqlInjection.fullDescription().get());
    assertEquals(ImmutableRule.Level.ERROR, sqlInjection.level().get());
    assertNotNull(sqlInjection.properties());
    final ImmutableRuleProperties ruleProperties = sqlInjection.properties().get();
    assertEquals("java/sql-injection", ruleProperties.id().get());
    assertEquals("Query built from user-controlled sources", ruleProperties.name().get());
    assertEquals(
        "Building a SQL or Java Persistence query from user-controlled sources is vulnerable to insertion of\n              malicious code by the user.",
        ruleProperties.description().get());
    assertEquals("high", ruleProperties.precision().get());
    assertEquals("path-problem", ruleProperties.kind().get());
    assertEquals(ImmutableRuleProperties.Severity.error, ruleProperties.severity().get());
    final ArrayList<String> esbTags = ruleProperties.tags().get();
    assertEquals(3, esbTags.size());
    assertEquals(1, esbTags.stream().filter("security"::equals).count());
    assertEquals(1, esbTags.stream().filter("external/cwe/cwe-089"::equals).count());
    assertEquals(1, esbTags.stream().filter("external/cwe/cwe-564"::equals).count());

    Mockito.verify(mockedParserCB, Mockito.times(1)).onFinding(resultCaptor.capture());
    final ImmutableResult result = resultCaptor.getValue();
    assertEquals("java/input-resource-leak", result.ruleId().get());
    assertEquals(64, result.ruleIndex().get());
    assertEquals("This FileReader is not always closed on method exit.", result.message().get());
    assertNotNull(result.locations());
    assertEquals(1, result.locations().get().size());
    final Location location = result.locations().get().get(0);
    assertEquals("src/main/java/com/baloise/open/maven/codeql/sarif/SarifParser.java", location.uri());
    assertEquals("%SRCROOT%", location.uriBaseId().get());
    assertEquals(0, location.index().get());
    assertNotNull(location.region().get());
    final Region region = location.region().get();
    assertEquals(84, region.startLine());
    assertEquals(58, region.startColumn().get());
    assertEquals(88, region.endColumn().get());
  }
}
