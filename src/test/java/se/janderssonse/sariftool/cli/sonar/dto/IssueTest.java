// SPDX-FileCopyrightText: 2021 Baloise Group
// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sariftool.cli.sonar.dto;

import org.junit.jupiter.api.Test;

import se.janderssonse.sariftool.cli.PropertyReflectionTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IssueTest extends PropertyReflectionTest {

  @Test
  void verifyProperties() {
    assertNumberOfProperties(ImmutableIssue.class, 7);
  }

  @Test
  void verifySeverityEnum() {
    assertEquals(5, ImmutableIssue.Severity.values().length);
    assertEquals(ImmutableIssue.Severity.BLOCKER, ImmutableIssue.Severity.valueOf("BLOCKER"));
    assertEquals(ImmutableIssue.Severity.CRITICAL, ImmutableIssue.Severity.valueOf("CRITICAL"));
    assertEquals(ImmutableIssue.Severity.MAJOR, ImmutableIssue.Severity.valueOf("MAJOR"));
    assertEquals(ImmutableIssue.Severity.MINOR, ImmutableIssue.Severity.valueOf("MINOR"));
    assertEquals(ImmutableIssue.Severity.INFO, ImmutableIssue.Severity.valueOf("INFO"));
  }

  @Test
  void verifyTypeEnum() {
    assertEquals(3, Issue.Type.values().length);
    assertEquals(Issue.Type.BUG, Issue.Type.valueOf("BUG"));
    assertEquals(Issue.Type.VULNERABILITY, Issue.Type.valueOf("VULNERABILITY"));
    assertEquals(Issue.Type.CODE_SMELL, Issue.Type.valueOf("CODE_SMELL"));
  }
}
