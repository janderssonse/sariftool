package se.janderssonse.sarifconvert.cli.sarif.dto;

import org.junit.jupiter.api.Test;

import se.janderssonse.sarifconvert.cli.PropertyReflectionTest;

import static org.junit.jupiter.api.Assertions.*;

class RuleTest extends PropertyReflectionTest {

  @Test
  void verifyProperties() {
    assertNumberOfProperties(Rule.class, 6);
  }

  @Test
  void verifyStringOutput() {
    final Rule testee = Rule.builder().build();
    assertEquals("Rule[null]: 'null'; null", testee.toString());
    testee.setId("MyTestId");
    assertEquals("Rule[MyTestId]: 'null'; null", testee.toString());
    testee.setLevel(Rule.Level.WARNING);
    assertEquals("Rule[MyTestId]-WARNING: 'null'; null", testee.toString());
    testee.setName("DaRulezName");
    assertEquals("Rule[MyTestId]-WARNING: 'DaRulezName'; null", testee.toString());
    testee.setProperties(RuleProperties.builder().build());
    assertEquals("Rule[MyTestId]-WARNING: 'DaRulezName'; RuleProperties{id='null', tags=null, kind='null', precision='null', severity=null}", testee.toString());
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
