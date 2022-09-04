// SPDX-FileCopyrightText: 2021 Baloise Group
// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sarifconvert.cli.sarif.dto;

import org.junit.jupiter.api.Test;

import se.janderssonse.sarifconvert.cli.PropertyReflectionTest;

import static org.junit.jupiter.api.Assertions.*;

class RuleTest extends PropertyReflectionTest {

  @Test
  void verifyProperties() {
    assertNumberOfProperties(ImmutableRule.class, 6);
  }

  @Test
  void verifyStringOutput() {
    ImmutableRule testee = ImmutableRule.builder().build();
    assertEquals("Rule[N/A]: 'N/A'; N/A", testee.toString());
    testee = ImmutableRule.builder().from(testee).id("MyTestId").build();
    assertEquals("Rule[MyTestId]: 'N/A'; N/A", testee.toString());
    testee = ImmutableRule.builder().from(testee).level(Rule.Level.WARNING).build();
    assertEquals("Rule[MyTestId]-WARNING: 'N/A'; N/A", testee.toString());
    testee = ImmutableRule.builder().from(testee).name("DaRulezName").build();
    assertEquals("Rule[MyTestId]-WARNING: 'DaRulezName'; N/A", testee.toString());
    testee = ImmutableRule.builder().from(testee).properties(ImmutableRuleProperties.builder().build()).build();
    assertEquals(
        "Rule[MyTestId]-WARNING: 'DaRulezName'; RuleProperties{id='', tags=[], kind='', precision='', severity=null}",
        testee.toString());
  }

  @Test
  void verifyEnum() {
    final Rule.Level[] levels = Rule.Level.values();
    assertEquals(4, levels.length);
    assertEquals(Rule.Level.WARNING, Rule.Level.valueOf("WARNING"));
    assertEquals(Rule.Level.ERROR, Rule.Level.valueOf("ERROR"));
    assertEquals(Rule.Level.NOTE, Rule.Level.valueOf("NOTE"));
    assertEquals(Rule.Level.NONE, Rule.Level.valueOf("NONE"));
  }

}
