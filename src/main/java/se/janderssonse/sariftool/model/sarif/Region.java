// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sariftool.model.sarif;

import java.util.Optional;

public record Region(Integer startLine, Optional<Integer> startColumn, Optional<Integer> endColumn) {

  public Region(final Optional<Integer> startColumn, final Optional<Integer> endColumn) {
    this(0, startColumn, endColumn);
  }

  public final String toString() {
    return String.format("Line %d, Column %d%s", startLine(), startColumn().orElse(0),
        endColumn().orElse(0) > 0 ? ":" + endColumn().get() : "");
  }
}
