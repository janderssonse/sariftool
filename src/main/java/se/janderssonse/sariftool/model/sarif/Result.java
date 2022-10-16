// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sariftool.model.sarif;

import java.util.List;
import java.util.Optional;

public record Result(Optional<String> ruleId,
    Optional<Integer> ruleIndex,
    Optional<String> message,
    Optional<List<Location>> locations) {

  @Override
  public final String toString() {
    String result = String.format("Found issue based on rule '%s': '%s'", ruleId().orElse("N/A"),
        message().orElse("N/A"));
    if (locations().isPresent() && !locations().get().isEmpty()) {
      result += "\nin '" + locations().get().get(0).toString() + "'";
    }
    return result;
  }

}
