// SPDX-FileCopyrightText: 2021 Baloise Group
// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sariftool;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import se.janderssonse.sariftool.mapper.Mapper;
import se.janderssonse.sariftool.model.sarif.Driver;
import se.janderssonse.sariftool.model.sarif.Location;
import se.janderssonse.sariftool.model.sarif.Region;
import se.janderssonse.sariftool.model.sarif.Result;
import se.janderssonse.sariftool.model.sarif.Rule;
import se.janderssonse.sariftool.model.sarif.RuleProperties;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class SarifParserTest {

  @Captor
  ArgumentCaptor<String> versionCaptor;

  @Captor
  ArgumentCaptor<String> schemaCaptor;

  @Captor
  ArgumentCaptor<Driver> driverCaptor;

  @Captor
  ArgumentCaptor<Rule> ruleCaptor;

  @Captor
  ArgumentCaptor<Result> resultCaptor;

  @Test
  @DisplayName("Non SARIF file does not invoke any callback function")
  void execute_NonSarifFile_CallbackNotInvoked() throws URISyntaxException, IOException {
    final Mapper mockedParserCB = Mockito.mock(Mapper.class);
    final Path exampleSarifFile = Paths.get(ClassLoader.getSystemResource("anyOther.json").toURI());

    final Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      SarifParser.map(exampleSarifFile, List.of(mockedParserCB), Paths.get(""), Collections.emptyList());
    });
    assertTrue(exception.getMessage().startsWith("Validation failed:"));

    assertAll(
        () -> Mockito.verify(mockedParserCB, Mockito.never()).onFinding(ArgumentMatchers.any(Result.class)),
        () -> Mockito.verify(mockedParserCB, Mockito.never()).onVersion(ArgumentMatchers.anyString()),
        () -> Mockito.verify(mockedParserCB, Mockito.never()).onSchema(ArgumentMatchers.anyString()),
        () -> Mockito.verify(mockedParserCB, Mockito.never()).onDriver(ArgumentMatchers.any(Driver.class)),
        () -> Mockito.verify(mockedParserCB, Mockito.never()).onRule(ArgumentMatchers.any(Rule.class)));
  }

  @Test
  @DisplayName("Happy Case parse example.sarif")
  void execute_testFile_HappyCase() throws URISyntaxException, IOException {
    final Mapper mockedParserCB = Mockito.mock(Mapper.class);
    final Path exampleSarifFile = Paths.get(ClassLoader.getSystemResource("example.sarif").toURI());

    SarifParser.map(exampleSarifFile, List.of(mockedParserCB), Paths.get(""), Collections.emptyList());

    Mockito.verify(mockedParserCB, Mockito.times(1)).onVersion(versionCaptor.capture());
    assertEquals("2.1.0", versionCaptor.getValue());

    Mockito.verify(mockedParserCB, Mockito.times(1)).onSchema(schemaCaptor.capture());
    assertEquals("https://raw.githubusercontent.com/oasis-tcs/sarif-spec/master/Schemata/sarif-schema-2.1.0.json",
        schemaCaptor.getValue());

    Mockito.verify(mockedParserCB, Mockito.times(1)).onDriver(driverCaptor.capture());
    verifyDriver(driverCaptor.getValue(), "2.3.3");

    Mockito.verify(mockedParserCB, Mockito.times(10)).onRule(ruleCaptor.capture());
    final List<Rule> rulesCaptured = ruleCaptor.getAllValues();
    verifyRule_EmptySynchBlock(rulesCaptured.get(2));
    verifyRule_impossibleArrayCast(rulesCaptured.get(3));

    Mockito.verify(mockedParserCB, Mockito.times(1)).onFinding(resultCaptor.capture());
    verifyResult(resultCaptor.getValue());
  }

  private void verifyResult(Result result) {
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
    assertEquals(Rule.Level.ERROR, impossibleArrayCast.level().get());
    assertNotNull(impossibleArrayCast.properties());
    final RuleProperties icProperties = impossibleArrayCast.properties().get();
    assertEquals("java/impossible-array-cast", icProperties.id().get());
    assertEquals("Impossible array cast", icProperties.name().get());
    assertEquals(
        "Trying to cast an array of a particular type as an array of a subtype causes a\n              'ClassCastException' at runtime.",
        icProperties.description().get());
    assertEquals("low", icProperties.precision().get());
    assertEquals("problem", icProperties.kind().get());
    assertEquals(RuleProperties.Severity.error, icProperties.severity().get());
    final ArrayList<String> icTags = icProperties.tags().get();
    assertEquals(4, icTags.size());
    assertEquals(1, icTags.stream().filter("reliability"::equals).count());
    assertEquals(1, icTags.stream().filter("correctness"::equals).count());
    assertEquals(1, icTags.stream().filter("logic"::equals).count());
    assertEquals(1, icTags.stream().filter("external/cwe/cwe-704"::equals).count());
  }

  private void verifyRule_EmptySynchBlock(Rule emptySynchBlock) {
    assertEquals("java/empty-synchronized-block", emptySynchBlock.id().get());
    assertEquals("java/empty-synchronized-block", emptySynchBlock.name().get());
    assertEquals("Empty synchronized block", emptySynchBlock.shortDescription().get());
    assertEquals(
        "Empty synchronized blocks may indicate the presence of incomplete code or incorrect synchronization, and may lead to concurrency problems.",
        emptySynchBlock.fullDescription().get());
    assertEquals(emptySynchBlock.level(), Optional.empty());
    assertNotNull(emptySynchBlock.properties().get());
    final RuleProperties esbProperties = emptySynchBlock.properties().get();
    assertEquals("java/empty-synchronized-block", esbProperties.id().get());
    assertEquals("Empty synchronized block", esbProperties.name().get());
    assertEquals(
        "Empty synchronized blocks may indicate the presence of\n              incomplete code or incorrect synchronization, and may lead to concurrency problems.",
        esbProperties.description().get());
    assertEquals("low", esbProperties.precision().get());
    assertEquals("problem", esbProperties.kind().get());
    assertEquals(RuleProperties.Severity.warning, esbProperties.severity().get());
    final ArrayList<String> esbTags = esbProperties.tags().get();
    assertEquals(5, esbTags.size());
    assertEquals(1, esbTags.stream().filter("reliability"::equals).count());
    assertEquals(1, esbTags.stream().filter("correctness"::equals).count());
    assertEquals(1, esbTags.stream().filter("concurrency"::equals).count());
    assertEquals(1, esbTags.stream().filter("language-features"::equals).count());
    assertEquals(1, esbTags.stream().filter("external/cwe/cwe-585"::equals).count());
  }

  private void verifyDriver(Driver driverCaptured, String versionExpected) {
    assertEquals("GitHub", driverCaptured.organization().get());
    assertEquals("CodeQL", driverCaptured.name().get());
    assertEquals(versionExpected, driverCaptured.semanticVersion().get());
  }

  @Test
  @DisplayName("Check FileNotFoundException on inexistent input file")
  void execute_wrongFile_FNFException() {
    final Mapper mockedParserCB = Mockito.mock(Mapper.class);
    assertThrows(FileNotFoundException.class,
        () -> SarifParser.map(Paths.get(""), List.of(mockedParserCB), Paths.get(""), Collections.emptyList()));
  }

  @Test
  @DisplayName("Verify extension rules are properly parsed using rulesInExtensions.sarif")
  void execute_rulesInExtensionsTest() throws URISyntaxException, IOException {
    final Mapper mockedParserCB = Mockito.mock(Mapper.class);
    final Path exampleSarifFile = Paths.get(ClassLoader.getSystemResource("rulesInExtensions.sarif").toURI());

    SarifParser.map(exampleSarifFile, List.of(mockedParserCB), Paths.get(""), Collections.emptyList());

    Mockito.verify(mockedParserCB, Mockito.times(1)).onVersion(versionCaptor.capture());
    assertEquals("2.1.0", versionCaptor.getValue());

    Mockito.verify(mockedParserCB, Mockito.times(1)).onSchema(schemaCaptor.capture());
    assertEquals("https://raw.githubusercontent.com/oasis-tcs/sarif-spec/master/Schemata/sarif-schema-2.1.0.json",
        schemaCaptor.getValue());

    Mockito.verify(mockedParserCB, Mockito.times(1)).onDriver(driverCaptor.capture());
    verifyDriver(driverCaptor.getValue(), "2.5.5");

    Mockito.verify(mockedParserCB, Mockito.times(166)).onRule(ruleCaptor.capture());
    final List<Rule> rulesCaptured = ruleCaptor.getAllValues();

    assertEquals(166, rulesCaptured.size());

    final Rule sqlInjection = rulesCaptured.get(0);
    assertEquals("java/sql-injection", sqlInjection.id().get());
    assertEquals("java/sql-injection", sqlInjection.name().get());
    assertEquals("Query built from user-controlled sources", sqlInjection.shortDescription().get());
    assertEquals(
        "Building a SQL or Java Persistence query from user-controlled sources is vulnerable to insertion of malicious code by the user.",
        sqlInjection.fullDescription().get());
    assertEquals(Rule.Level.ERROR, sqlInjection.level().get());
    assertNotNull(sqlInjection.properties());
    final RuleProperties ruleProperties = sqlInjection.properties().get();
    assertEquals("java/sql-injection", ruleProperties.id().get());
    assertEquals("Query built from user-controlled sources", ruleProperties.name().get());
    assertEquals(
        "Building a SQL or Java Persistence query from user-controlled sources is vulnerable to insertion of\n              malicious code by the user.",
        ruleProperties.description().get());
    assertEquals("high", ruleProperties.precision().get());
    assertEquals("path-problem", ruleProperties.kind().get());
    assertEquals(RuleProperties.Severity.error, ruleProperties.severity().get());
    final ArrayList<String> esbTags = ruleProperties.tags().get();
    assertEquals(3, esbTags.size());
    assertEquals(1, esbTags.stream().filter("security"::equals).count());
    assertEquals(1, esbTags.stream().filter("external/cwe/cwe-089"::equals).count());
    assertEquals(1, esbTags.stream().filter("external/cwe/cwe-564"::equals).count());

    Mockito.verify(mockedParserCB, Mockito.times(1)).onFinding(resultCaptor.capture());
    final Result result = resultCaptor.getValue();
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
