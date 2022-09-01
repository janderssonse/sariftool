package se.janderssonse.sarifconvert.cli.sonar.dto;

import org.junit.jupiter.api.Test;

import se.janderssonse.sarifconvert.cli.PropertyReflectionTest;

class IssuesTest extends PropertyReflectionTest {

  @Test
  void verifyProperties() {
    assertNumberOfProperties(Issues.class, 1);
  }


}
