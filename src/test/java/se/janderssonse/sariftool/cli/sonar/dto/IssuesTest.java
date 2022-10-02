// SPDX-FileCopyrightText: 2021 Baloise Group
// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sariftool.cli.sonar.dto;

import org.junit.jupiter.api.Test;

import se.janderssonse.sariftool.cli.PropertyReflectionTest;

class IssuesTest extends PropertyReflectionTest {

  @Test
  void verifyProperties() {
    assertNumberOfProperties(Issues.class, 1);
  }


}
