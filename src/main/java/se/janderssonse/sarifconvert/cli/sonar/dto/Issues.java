/*
 Copyright 2021 Baloise Group

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
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
  private final List<Issue> result = new ArrayList<>();

  public List<Issue> getResult() {
    return result;
  }

  public Issues applyFilter(Predicate<Issue> predicate) {
    final Issues filteredIssues = new Issues();
    filteredIssues.getResult().addAll(this.getResult().stream().filter(predicate).collect(Collectors.toList()));
    return filteredIssues;
  }
}
