// SPDX-FileCopyrightText: 2021 Baloise Group
// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sarifconvert.cli.sonar.dto;

import java.util.Optional;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class TextRange {

  private Integer startLine;
  private Integer endLine;
  private Integer startColumn;
  private Integer endColumn;

}
