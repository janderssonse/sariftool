// SPDX-FileCopyrightText: 2021 Baloise Group
// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sariftool.cli.sonar.dto;

import java.util.Optional;

import org.immutables.value.Value;

@Value.Immutable
public abstract class TextRange {

  public abstract Integer startLine();
  public abstract Integer endLine();
  public abstract Optional<Integer> startColumn();
  public abstract Optional<Integer> endColumn();

}
