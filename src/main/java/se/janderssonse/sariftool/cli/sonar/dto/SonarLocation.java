// SPDX-FileCopyrightText: 2021 Baloise Group
// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sariftool.cli.sonar.dto;

import org.immutables.value.Value;

@Value.Immutable
public abstract class SonarLocation {

  public abstract String message();
  public abstract String filePath();
  public abstract ImmutableTextRange textRange();

}
