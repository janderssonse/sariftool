// SPDX-FileCopyrightText: 2021 Baloise Group
// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sarifconvert.cli.sarif.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder(toBuilder = true)
public class Region {

  @Builder.Default
  private Integer startLine = 0;
  @Builder.Default
  private Integer startColumn = 0;
  @Builder.Default
  private Integer endColumn = 0;

  @Override
  public String toString() {
    return String.format("Line %d, Column %d%s", startLine, startColumn, endColumn != null && endColumn > 0 ? ":" + endColumn : "");
  }
}
