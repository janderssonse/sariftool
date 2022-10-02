// SPDX-FileCopyrightText: 2021 Baloise Group
// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sariftool.cli.sarif.dto;

import java.util.Optional;

import org.immutables.value.Value;

@Value.Immutable
public abstract class Region {

  /**
   * Default value for startline.
   * @return An integer
   */
  @Value.Default
  public Integer startLine() {
    return 0;
  }

  public abstract Optional<Integer> startColumn();

  public abstract Optional<Integer> endColumn();

  public final String toString() {
    return String.format("Line %d, Column %d%s", startLine(), startColumn().orElse(0),
        endColumn().orElse(0) > 0 ? ":" + endColumn().get() : "");
  }
}
