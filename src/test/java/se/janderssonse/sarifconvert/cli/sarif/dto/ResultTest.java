package se.janderssonse.sarifconvert.cli.sarif.dto;

import org.junit.jupiter.api.Test;

import se.janderssonse.sarifconvert.cli.PropertyReflectionTest;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class ResultTest extends PropertyReflectionTest {

  @Test
  void verifyProperties() {
    assertNumberOfProperties(Result.class, 4);
  }

  @Test
  void verifyStringOutput() {
    final Result testee = Result.builder().build();
    assertEquals("Found issue based on rule 'null': 'null'", testee.toString());
    testee.setRuleId("MyRuleId/x");
    assertEquals("Found issue based on rule 'MyRuleId/x': 'null'", testee.toString());
    testee.setMessage("To test the message setter/getter");
    assertEquals("Found issue based on rule 'MyRuleId/x': 'To test the message setter/getter'", testee.toString());
    testee.setLocations(Collections.emptyList());
    assertEquals("Found issue based on rule 'MyRuleId/x': 'To test the message setter/getter'", testee.toString());
    testee.setLocations(Collections.singletonList(Location.builder().build()));
    assertEquals("Found issue based on rule 'MyRuleId/x': 'To test the message setter/getter'\nin '<URI_MISSING>, n/a'", testee.toString());
  }

}
