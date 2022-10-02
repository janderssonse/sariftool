// SPDX-FileCopyrightText: 2021 Baloise Group
// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sariftool.cli;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class PropertyReflectionTest {

  protected void assertNumberOfProperties(Class<?> className, long expectedCount) {
    final Field[] declaredFields = className.getDeclaredFields();
    assertEquals(expectedCount, Arrays.stream(declaredFields).filter(field -> !field.getName().contains("$jacocoData")).count());
  }

}
