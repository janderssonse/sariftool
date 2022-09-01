// SPDX-FileCopyrightText: 2021 Baloise Group
// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sarifconvert.cli.sarif.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder(toBuilder = true)
public class Result {

  private String ruleId;
  private Integer ruleIndex;
  private String message;
  private List<Location> locations;

  @Override
  public String toString() {
    String result = String.format("Found issue based on rule '%s': '%s'", ruleId, message);
    if (locations != null && !locations.isEmpty()) {
      result += "\nin '" + locations.get(0).toString() + "'";
    }
    return result;
  }

}
