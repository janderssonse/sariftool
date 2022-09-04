// SPDX-FileCopyrightText: 2021 Baloise Group
// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sarifconvert.cli.sonar.dto;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Main object of exported Sonar Issue Report.
 * See also Generic Issue Import Format https://docs.sonarqube.org/latest/analysis/generic-issue/
 */
public final class Issues {

  @SerializedName(value = "issues")
  private final List<ImmutableIssue> result = new ArrayList<>();

  public List<ImmutableIssue> getResult() {
    return result;
  }

  public Issues applyFilter(Predicate<ImmutableIssue> predicate) {
    final Issues filteredIssues = new Issues();
    filteredIssues.getResult().addAll(this.getResult().stream().filter(predicate).collect(Collectors.toList()));
    return filteredIssues;
  }
}
