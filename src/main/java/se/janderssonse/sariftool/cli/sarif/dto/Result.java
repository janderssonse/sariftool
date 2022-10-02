// SPDX-FileCopyrightText: 2021 Baloise Group
// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sariftool.cli.sarif.dto;

import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

@Value.Immutable
public abstract class Result {

  public abstract Optional<String> ruleId();

  public abstract Optional<Integer> ruleIndex();

  public abstract Optional<String> message();

  public abstract Optional<List<ImmutableLocation>> locations();

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
