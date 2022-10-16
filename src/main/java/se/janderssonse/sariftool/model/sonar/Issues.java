// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sariftool.model.sonar;



import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Main object of exported Sonar Issue Report.
 * See also Generic Issue Import Format https://docs.sonarqube.org/latest/analysis/generic-issue/
 */
public final class Issues {

  @Override
  public String toString() {
    return "Issues []" + result.stream().map(it -> it.toString()).collect(Collectors.joining(","));
  }

  @JsonProperty(value = "issues")
  private final List<Issue> result = new ArrayList<>();

  public List<Issue> getResult() {
    return result;
  }

  public Issues applyFilter(final Predicate<Issue> predicate) {
    final Issues filteredIssues = new Issues();
    filteredIssues.getResult().addAll(this.getResult().stream().filter(predicate).collect(Collectors.toList()));
    return filteredIssues;
  }
}
