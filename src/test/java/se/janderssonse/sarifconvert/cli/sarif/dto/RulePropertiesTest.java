// SPDX-FileCopyrightText: 2021 Baloise Group
// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sarifconvert.cli.sarif.dto;

import org.junit.jupiter.api.Test;

import se.janderssonse.sarifconvert.cli.PropertyReflectionTest;

import static org.junit.jupiter.api.Assertions.*;

class RulePropertiesTest extends PropertyReflectionTest {

  @Test
  void verifyProperties() {
    assertNumberOfProperties(RuleProperties.class, 7);
  }

  @Test
  void verifyStringOutput() {
    final RuleProperties testee = RuleProperties.builder().build();
    assertEquals("RuleProperties{id='null', tags=null, kind='null', precision='null', severity=null}", testee.toString());
    testee.setId("MyFancyRuleId/xyz");
    assertEquals("RuleProperties{id='MyFancyRuleId/xyz', tags=null, kind='null', precision='null', severity=null}", testee.toString());
    testee.setTags(new String[]{"Tag1","Tag2"});
    assertEquals("RuleProperties{id='MyFancyRuleId/xyz', tags=[Tag1, Tag2], kind='null', precision='null', severity=null}", testee.toString());
    testee.setTags(new String[]{"Tag1"});
    assertEquals("RuleProperties{id='MyFancyRuleId/xyz', tags=[Tag1], kind='null', precision='null', severity=null}", testee.toString());
    testee.setKind("AnyKind");
    assertEquals("RuleProperties{id='MyFancyRuleId/xyz', tags=[Tag1], kind='AnyKind', precision='null', severity=null}", testee.toString());
    testee.setPrecision("Precision1");
    assertEquals("RuleProperties{id='MyFancyRuleId/xyz', tags=[Tag1], kind='AnyKind', precision='Precision1', severity=null}", testee.toString());
    testee.setSeverity(RuleProperties.Severity.recommendation);
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
