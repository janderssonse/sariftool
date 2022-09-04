// SPDX-FileCopyrightText: 2021 Baloise Group
// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sarifconvert.cli.sarif.dto;

import org.junit.jupiter.api.Test;

import se.janderssonse.sarifconvert.cli.PropertyReflectionTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LocationTest extends PropertyReflectionTest {

  @Test
  void verifyProperties() {
    assertNumberOfProperties(ImmutableLocation.class, 4);
  }

  @Test
  void verifyStringOutput() {
     Location testee = ImmutableLocation.builder().build();
    assertEquals("<URI_MISSING>, n/a", testee.toString());
    testee = ImmutableLocation.builder().from(testee).uri("aFanyButWrongURI").build();
    assertEquals("aFanyButWrongURI, n/a", testee.toString());
    ;
    testee = ImmutableLocation.builder().from(testee).region(ImmutableRegion.builder().build()).uri("aFanyButWrongURI").build();
    assertEquals("aFanyButWrongURI, Line 0, Column 0", testee.toString());
  }

}
