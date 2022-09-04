// SPDX-FileCopyrightText: 2021 Baloise Group
// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sarifconvert.cli.sarif.dto;

import org.junit.jupiter.api.Test;

import se.janderssonse.sarifconvert.cli.PropertyReflectionTest;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class ResultTest extends PropertyReflectionTest {

  @Test
  void verifyProperties() {
    assertNumberOfProperties(ImmutableResult.class, 4);
  }

  @Test
  void verifyStringOutput() {
    ImmutableResult testee = ImmutableResult.builder().build();
    assertEquals("Found issue based on rule 'N/A': 'N/A'", testee.toString());
    testee = ImmutableResult.builder().from(testee).ruleId("MyRuleId/x").build();
    assertEquals("Found issue based on rule 'MyRuleId/x': 'N/A'", testee.toString());
    testee = ImmutableResult.builder().from(testee).message("To test the message setter/getter").build();
    assertEquals("Found issue based on rule 'MyRuleId/x': 'To test the message setter/getter'", testee.toString());
    testee = ImmutableResult.builder().from(testee).locations(Collections.emptyList()).build();
    assertEquals("Found issue based on rule 'MyRuleId/x': 'To test the message setter/getter'", testee.toString());
    testee = ImmutableResult.builder().from(testee).locations(Collections.singletonList(ImmutableLocation.builder().build())).build();
    assertEquals("Found issue based on rule 'MyRuleId/x': 'To test the message setter/getter'\nin '<URI_MISSING>, n/a'", testee.toString());
  }

}
