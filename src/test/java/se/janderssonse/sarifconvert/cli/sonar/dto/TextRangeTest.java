package se.janderssonse.sarifconvert.cli.sonar.dto;

import org.junit.jupiter.api.Test;

import se.janderssonse.sarifconvert.cli.PropertyReflectionTest;

class TextRangeTest extends PropertyReflectionTest {

  @Test
  void verifyProperties() {
    assertNumberOfProperties(TextRange.class, 4);
  }

}
