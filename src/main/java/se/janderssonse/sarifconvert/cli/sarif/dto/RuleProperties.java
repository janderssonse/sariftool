// SPDX-FileCopyrightText: 2021 Baloise Group
// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sarifconvert.cli.sarif.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@Getter
@Setter
@Builder(toBuilder = true)
public class RuleProperties {

  private String id;
  private String name;
  private String description;
  private String[] tags;
  private String kind;
  private String precision;
  private Severity severity;

  @Override
  public String toString() {
    return "RuleProperties{" +
            "id='" + id + '\'' +
            ", tags=" + Arrays.toString(tags) +
            ", kind='" + kind + '\'' +
            ", precision='" + precision + '\'' +
            ", severity=" + severity +
            '}';
  }

  public enum Severity {
    warning,
    error,
    recommendation
  }
}
