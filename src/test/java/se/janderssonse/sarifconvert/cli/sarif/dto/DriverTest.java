// SPDX-FileCopyrightText: 2021 Baloise Group
// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sarifconvert.cli.sarif.dto;

import org.junit.jupiter.api.Test;

import se.janderssonse.sarifconvert.cli.PropertyReflectionTest;

import static org.junit.jupiter.api.Assertions.*;

class DriverTest extends PropertyReflectionTest {

  @Test
  void verifyProperties() {
    assertNumberOfProperties(ImmutableDriver.class, 3);
  }

  @Test
  void verifyStringOutput() {
    ImmutableDriver testee = ImmutableDriver.builder().build();
    assertEquals("n/a n/a", testee.toString());
    testee = ImmutableDriver.builder().from(testee).organization("DriverOrg").build();
    assertEquals("DriverOrg n/a", testee.toString());
    testee = ImmutableDriver.builder().from(testee).name("DriverName").build();
    assertEquals("DriverOrg DriverName", testee.toString());
    testee = ImmutableDriver.builder().from(testee).semanticVersion("1.2.3").build();
    assertEquals("DriverOrg DriverName v1.2.3", testee.toString());
  }
}
