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
    assertNumberOfProperties(Driver.class, 3);
  }

  @Test
  void verifyStringOutput() {
    final Driver testee = Driver.builder().build();
    assertEquals("n/a n/a", testee.toString());
    testee.setOrganization("DriverOrg");
    assertEquals("DriverOrg n/a", testee.toString());
    testee.setName("DriverName");
    assertEquals("DriverOrg DriverName", testee.toString());
    testee.setSemanticVersion("1.2.3");
    assertEquals("DriverOrg DriverName v1.2.3", testee.toString());
  }
}
