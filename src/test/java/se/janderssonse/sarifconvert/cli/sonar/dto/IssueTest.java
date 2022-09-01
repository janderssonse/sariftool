// SPDX-FileCopyrightText: 2021 Baloise Group
// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sarifconvert.cli.sonar.dto;

import org.junit.jupiter.api.Test;

import se.janderssonse.sarifconvert.cli.PropertyReflectionTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IssueTest extends PropertyReflectionTest {

  @Test
  void verifyProperties() {
    assertNumberOfProperties(Issue.class, 7);
  }

  @Test
  void verifySeverityEnum() {
    assertEquals(5, Issue.Severity.values().length);
    assertEquals(Issue.Severity.BLOCKER, Issue.Severity.valueOf("BLOCKER"));
    assertEquals(Issue.Severity.CRITICAL, Issue.Severity.valueOf("CRITICAL"));
    assertEquals(Issue.Severity.MAJOR, Issue.Severity.valueOf("MAJOR"));
    assertEquals(Issue.Severity.MINOR, Issue.Severity.valueOf("MINOR"));
    assertEquals(Issue.Severity.INFO, Issue.Severity.valueOf("INFO"));
  }

  @Test
  void verifyTypeEnum() {
    assertEquals(3, Issue.Type.values().length);
    assertEquals(Issue.Type.BUG, Issue.Type.valueOf("BUG"));
    assertEquals(Issue.Type.VULNERABILITY, Issue.Type.valueOf("VULNERABILITY"));
    assertEquals(Issue.Type.CODE_SMELL, Issue.Type.valueOf("CODE_SMELL"));
  }
}
