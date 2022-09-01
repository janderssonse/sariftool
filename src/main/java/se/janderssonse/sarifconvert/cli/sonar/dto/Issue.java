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

import lombok.Builder;
import lombok.Data;

import java.util.Set;

/**
 * This class represents the structure of sonar's generic issue import format
 * see: <br />
 * - https://docs.sonarqube.org/latest/analysis/generic-issue/
 * - https://docs.sonarqube.org/latest/user-guide/issues/
 */
@Data
@Builder(toBuilder = true)
public class Issue {

  private String engineId;
  private String ruleId;
  private Severity severity;
  private Type type;
  private Location primaryLocation;
  private Set<Location> secondaryLocations;
  private int effortMinutes;

  public enum Severity {
    /**
     * Bug with a high probability to impact the behavior of the application in production: memory leak, unclosed JDBC connection, .... The code MUST be immediately fixed.
     */
    BLOCKER,
    /**
     * Either a bug with a low probability to impact the behavior of the application in production or an issue which represents a security flaw: empty catch block, SQL injection, ... The code MUST be immediately reviewed.
     */
    CRITICAL,
    /**
     * Quality flaw which can highly impact the developer productivity: uncovered piece of code, duplicated blocks, unused parameters, ...
     */
    MAJOR,
    /**
     * Quality flaw which can slightly impact the developer productivity: lines should not be too long, "switch" statements should have at least 3 cases, ...
     */
    MINOR,
    /**
     * Neither a bug nor a quality flaw, just a finding.
     */
    INFO
  }

  public enum Type {
    /**
     * A coding error that will break your code and needs to be fixed immediately.
     */
    BUG,
    /**
     * A point in your code that's open to attack.
     */
    VULNERABILITY,
    /**
     * A maintainability issue that makes your code confusing and difficult to maintain.
     */
    CODE_SMELL
  }
}
