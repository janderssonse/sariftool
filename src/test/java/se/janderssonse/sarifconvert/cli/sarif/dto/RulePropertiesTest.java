// SPDX-FileCopyrightText: 2021 Baloise Group
// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sarifconvert.cli.sarif.dto;

import org.junit.jupiter.api.Test;

import se.janderssonse.sarifconvert.cli.PropertyReflectionTest;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

class RulePropertiesTest extends PropertyReflectionTest {

  @Test
  void verifyProperties() {
    assertNumberOfProperties(ImmutableRuleProperties.class, 7);
  }

  @Test
  void verifyStringOutput() {
    ImmutableRuleProperties testee = ImmutableRuleProperties.builder().build();
    assertEquals("RuleProperties{id='', tags=[], kind='', precision='', severity=null}", testee.toString());
    testee = ImmutableRuleProperties.builder().from(testee).id("MyFancyRuleId/xyz").build();
    assertEquals("RuleProperties{id='MyFancyRuleId/xyz', tags=[], kind='', precision='', severity=null}", testee.toString());
    testee = ImmutableRuleProperties.builder().from(testee).tags(new ArrayList<>(List.of("Tag1","Tag2"))).build();
    assertEquals("RuleProperties{id='MyFancyRuleId/xyz', tags=[Tag1, Tag2], kind='', precision='', severity=null}", testee.toString());
    testee = ImmutableRuleProperties.builder().from(testee).tags(new ArrayList<>(List.of("Tag1"))).build();
    assertEquals("RuleProperties{id='MyFancyRuleId/xyz', tags=[Tag1], kind='', precision='', severity=null}", testee.toString());
    testee = ImmutableRuleProperties.builder().from(testee).kind("AnyKind").build();
    assertEquals("RuleProperties{id='MyFancyRuleId/xyz', tags=[Tag1], kind='AnyKind', precision='', severity=null}", testee.toString());
    testee = ImmutableRuleProperties.builder().from(testee).precision("Precision1").build();
    assertEquals("RuleProperties{id='MyFancyRuleId/xyz', tags=[Tag1], kind='AnyKind', precision='Precision1', severity=null}", testee.toString());
    testee = ImmutableRuleProperties.builder().from(testee).severity(ImmutableRuleProperties.Severity.recommendation).build();
    assertEquals("RuleProperties{id='MyFancyRuleId/xyz', tags=[Tag1], kind='AnyKind', precision='Precision1', severity=recommendation}", testee.toString());
  }

  @Test
  void verifyEnum() {
    assertEquals(3, RuleProperties.Severity.values().length);
    assertEquals(RuleProperties.Severity.warning, RuleProperties.Severity.valueOf("warning"));
    assertEquals(RuleProperties.Severity.error, RuleProperties.Severity.valueOf("error"));
    assertEquals(RuleProperties.Severity.recommendation, RuleProperties.Severity.valueOf("recommendation"));
  }
}
