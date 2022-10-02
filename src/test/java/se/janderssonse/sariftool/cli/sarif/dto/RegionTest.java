// SPDX-FileCopyrightText: 2021 Baloise Group
// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sariftool.cli.sarif.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import se.janderssonse.sariftool.cli.PropertyReflectionTest;


class RegionTest extends PropertyReflectionTest {

  @Test
  void verifyProperties() {
    assertNumberOfProperties(ImmutableRegion.class, 3);
  }

  @Test
  void verifyStringOutput() {

    Region testee = ImmutableRegion.builder()
        .build();

    assertEquals("Line 0, Column 0", testee.toString());
    testee = ImmutableRegion.builder()
        .from(testee)
        .startLine(75).build();
    assertEquals("Line 75, Column 0", testee.toString());
    testee = ImmutableRegion.builder()
        .from(testee)
        .startColumn(9)
        .build();
    assertEquals("Line 75, Column 9", testee.toString());
    testee = ImmutableRegion.builder()
        .from(testee)
        .endColumn(14).build();
    assertEquals("Line 75, Column 9:14", testee.toString());
  }

}
