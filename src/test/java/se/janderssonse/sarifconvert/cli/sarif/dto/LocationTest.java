package se.janderssonse.sarifconvert.cli.sarif.dto;

import org.junit.jupiter.api.Test;

import se.janderssonse.sarifconvert.cli.PropertyReflectionTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Disabled;

class LocationTest extends PropertyReflectionTest {

  @Test
  void verifyProperties() {
    assertNumberOfProperties(Location.class, 4);
  }

  @Test
  void verifyStringOutput() {
    final Location testee = Location.builder().build();
    assertEquals("<URI_MISSING>, n/a", testee.toString());
    testee.setUri("aFanyButWrongURI");
    assertEquals("aFanyButWrongURI, n/a", testee.toString());
    testee.setRegion(Region.builder().build());
    assertEquals("aFanyButWrongURI, Line 0, Column 0", testee.toString());
  }

}
