// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sariftool.model.sonar;

import java.util.Optional;
import java.util.Set;

/*
 * This class represents the structure of sonar's generic issue import format.
 * <br />
 * - https://docs.sonarqube.org/latest/analysis/generic-issue/
 * - https://docs.sonarqube.org/latest/user-guide/issues/
 */
public record Issue(
    Optional<String> engineId,
    Optional<String> ruleId,
    Optional<Severity> severity,
    Optional<Type> type,
    Optional<SonarLocation> primaryLocation,
    Optional<Set<SonarLocation>> secondaryLocations,
    Optional<Integer> effortMinutes) {

  public enum Severity {
    /*
     * Bug with a high probability to impact the behavior of the application in
     * production: memory leak, unclosed JDBC connection, .... The code MUST be
     * immediately fixed.
     */
    BLOCKER,
    /*
     * Either a bug with a low probability to impact the behavior of the application
     * in production or an issue which represents a security flaw: empty catch
     * block, SQL injection, ... The code MUST be immediately reviewed.
     */
    CRITICAL,
    /*
     * Quality flaw which can highly impact the developer productivity: uncovered
     * piece of code, duplicated blocks, unused parameters, ...
     */
    MAJOR,
    /*
     * Quality flaw which can slightly impact the developer productivity: lines
     * should not be too long, "switch" statements should have at least 3 cases, ...
     */
    MINOR,
    /*
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
     * A maintainability issue that makes your code confusing and difficult to
     * maintain.
     */
    CODE_SMELL
  }
}
