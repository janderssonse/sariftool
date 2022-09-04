// SPDX-FileCopyrightText: 2021 Baloise Group
// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sarifconvert.cli;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class SonarIssueReporterTest {

  @Test
  void execute_FileMissing_ExceptionExpected() {
    assertThrows(RuntimeException.class, () -> new SonarIssueReporter(null, null, false, null).execute());
    assertThrows(RuntimeException.class, () -> new SonarIssueReporter("   ", null, false, null).execute());
    assertThrows(RuntimeException.class, () -> new SonarIssueReporter(null, "   ", false, null).execute());
    assertThrows(RuntimeException.class, () -> new SonarIssueReporter("   ", "   ", false, null).execute());
  }

  @Test
  void testInvalidSarifFile() {
    final File input = new File("src/test/resources/anyOther.json");
    assertTrue(input.isFile());

    final SonarIssueReporter testee = new SonarIssueReporter(input.getAbsolutePath());
    final Exception exception = assertThrows(Exception.class, testee::execute);
    assertTrue(exception.getMessage().startsWith("$schema not found in root object"));
  }


  @Test
  void testHappyCase() throws Exception, IOException {
    testByResourceFiles("src/test/resources/example.sarif", "src/test/resources/expectedResult.json", false);
  }

  @Test
  void testWithFilter() throws Exception, IOException {
    testByResourceFiles("src/test/resources/InputWithTestResource.sarif", "src/test/resources/expectedResult.json", true);
  }

  @Test
  void multiModuleTest() throws Exception, IOException {
    testByResourceFiles("src/test/resources/multiModuleInput.sarif", "src/test/resources/multiModuleResult.json", false);
  }

  private void testByResourceFiles(String inputSarifFIle, String expectedResultFile, boolean ignoreTests) throws Exception, IOException {
    final File input = new File(inputSarifFIle);
    assertTrue(input.isFile());
    final File expected = new File(expectedResultFile);
    assertTrue(expected.isFile());

    final SonarIssueReporter testee = new SonarIssueReporter(input.getAbsolutePath(), null, ignoreTests, null);
    final StringWriter testwriter = new StringWriter();
    testee.setWriter(testwriter);
    testee.execute();

    final String expectedString = Files.readString(expected.toPath()).trim().replace("\n", "").replace("\r", "");
    String other = testwriter.toString().trim().replace("\n", "").replace("\r", "");
    assertEquals(expectedString,other);
  }


}
