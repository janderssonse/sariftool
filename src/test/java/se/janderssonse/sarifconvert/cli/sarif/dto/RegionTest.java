package se.janderssonse.sarifconvert.cli.sarif.dto;

import org.junit.jupiter.api.Test;

import se.janderssonse.sarifconvert.cli.PropertyReflectionTest;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

class RegionTest extends PropertyReflectionTest {

  @Test
  void verifyProperties() {
    assertNumberOfProperties(Region.class, 3);
  }

  @Test
  void verifyStringOutput() {
    final Region testee = Region.builder().build();
    assertEquals("Line 0, Column 0", testee.toString());
    testee.setStartLine(75);
    assertEquals("Line 75, Column 0", testee.toString());
    testee.setStartColumn(9);
    assertEquals("Line 75, Column 9", testee.toString());
    testee.setEndColumn(14);
    assertEquals("Line 75, Column 9:14", testee.toString());
  }

}
