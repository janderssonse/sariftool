// SPDX-FileCopyrightText: 2021 Baloise Group
// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sariftool.cli.sonar;

import se.janderssonse.sariftool.cli.sarif.dto.ImmutableDriver;
import se.janderssonse.sariftool.cli.sarif.dto.ImmutableLocation;
import se.janderssonse.sariftool.cli.sarif.dto.ImmutableRegion;
import se.janderssonse.sariftool.cli.sarif.dto.ImmutableResult;
import se.janderssonse.sariftool.cli.sarif.dto.ImmutableRule;
import se.janderssonse.sariftool.cli.sarif.dto.ImmutableRuleProperties;
import se.janderssonse.sariftool.cli.sonar.dto.ImmutableIssue;
import se.janderssonse.sariftool.cli.sonar.dto.ImmutableSonarLocation;
import se.janderssonse.sariftool.cli.sonar.dto.ImmutableTextRange;
import se.janderssonse.sariftool.cli.sarif.ParserCallback;
import se.janderssonse.sariftool.cli.sonar.dto.Issues;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class SonarIssueMapper implements ParserCallback {

  private final List<ImmutableResult> results = new ArrayList<>();
  private List<ImmutableRule> rules = new ArrayList<>();
  private final Issues mappedIssues = new Issues();
  private ImmutableDriver driver;

  public void setRules(final List<ImmutableRule> rules) {
    this.rules = rules;
  }

  public void setDriver(final ImmutableDriver driver) {
    this.driver = driver;
  }

  public String getVersion() {
    return version;
  }

  public String getSchema() {
    return schema;
  }

  private String version;
  private String schema;

  @Override
  public void onFinding(final ImmutableResult result) {
    // To do check if result valid¨
    if (result != null) {
      results.add(result);
      mappedIssues.getResult().add(mapResult(result));
    }
  }

  @Override
  public void onVersion(final String version) {
    this.version = version;
  }

  @Override
  public void onSchema(final String schema) {
    this.schema = schema;
  }

  @Override
  public void onDriver(final ImmutableDriver driver) {
    this.driver = driver;
  }

  @Override
  public void onRule(final ImmutableRule rule) {
    rules.add(rule);
  }

  public void finding(final ImmutableResult result) {
    this.onFinding(result);
  }

  private ImmutableIssue mapResult(final ImmutableResult result) {
    final ImmutableIssue.Severity severity = mapSeverity(result.ruleId().get());
    return ImmutableIssue.builder()
        .ruleId(result.ruleId().get())
        .primaryLocation(mapPrimaryLocation(result))
        .secondaryLocations(mapSecondaryLocations(result))
        .severity(severity)
        .type(mapType(severity))
        .effortMinutes(0)
        .engineId(driver != null ? driver.toString() : SonarIssueMapper.class.getSimpleName())
        .build();
  }

  ImmutableIssue.Severity mapSeverity(final String ruleId) {
    final ImmutableRule matchingRule = rules.stream().filter(rule -> rule.id().get().equals(ruleId)).findFirst()
        .orElse(null);
    if (matchingRule != null && matchingRule.properties().isPresent()
        && matchingRule.properties().get().severity().isPresent()) {
      return mapRuleToIssueSeverity(matchingRule.level().orElse(null), matchingRule.properties());
    }
    return ImmutableIssue.Severity.INFO;
  }

  ImmutableIssue.Severity mapRuleToIssueSeverity(final ImmutableRule.Level level,
      final Optional<ImmutableRuleProperties> properties) {
    if (properties.isEmpty() || properties.get().severity().isEmpty()) {
      // without properties the only basis to map severity is the rule level.
      return (level == null) ? null : mapRuleLevelToSeverity(level);
    }

    final ImmutableRuleProperties.Severity ruleSeverity = properties.get().severity().get();
    final Optional<String> rulePrecision = properties.get().precision();

    switch (ruleSeverity) {
      case recommendation:
        return ImmutableIssue.Severity.INFO;
      case warning:
        // consider precision as first criteria
        return mapRuleSeverityWarning(level, rulePrecision);
      case error:
        // consider precision as first criteria
        return mapRuleSeverityError(level, rulePrecision);
      default:
        return null;
    }
  }

  private ImmutableIssue.Severity mapRuleSeverityError(final ImmutableRule.Level level,
      final Optional<String> rulePrecision) {
    if (rulePrecision.isPresent()) {
      switch (rulePrecision.get().toLowerCase()) {
        case "medium":
        case "high":
          return ImmutableIssue.Severity.CRITICAL;
        case "very-high":
          return ImmutableIssue.Severity.BLOCKER;
        default:
          // not decisive yet
      }
    }
    // if not set or unknown consider level as second criteria
    if (level == ImmutableRule.Level.ERROR) {
      return ImmutableIssue.Severity.BLOCKER;
    }
    return ImmutableIssue.Severity.CRITICAL;
  }

  private ImmutableIssue.Severity mapRuleSeverityWarning(final ImmutableRule.Level level,
      final Optional<String> rulePrecision) {
    if (rulePrecision.isPresent()) {
      switch (rulePrecision.get().toLowerCase()) {
        case "medium":
          return ImmutableIssue.Severity.MINOR;
        case "high":
          return ImmutableIssue.Severity.MAJOR;
        case "very-high":
          return ImmutableIssue.Severity.CRITICAL;
        default:
          // not decisive yet
      }
    }
    // if not set or unknown consider level as second criteria
    return (level == null) ? ImmutableIssue.Severity.MINOR : mapRuleLevelToSeverity(level);
  }

  private ImmutableIssue.Severity mapRuleLevelToSeverity(final ImmutableRule.Level level) {
    switch (level) {
      case NONE:
      case NOTE:
        return ImmutableIssue.Severity.MINOR;
      case WARNING:
        return ImmutableIssue.Severity.MAJOR;
      case ERROR:
        return ImmutableIssue.Severity.CRITICAL;
      default:
        return null;
    }
  }

  ImmutableIssue.Type mapType(final ImmutableIssue.Severity severity) {
    if (severity == null) {
      return null;
    }
    switch (severity) {
      case INFO:
      case MINOR:
      case MAJOR:
        return ImmutableIssue.Type.CODE_SMELL;
      case BLOCKER:
        return ImmutableIssue.Type.BUG;
      case CRITICAL:
      default:
        return ImmutableIssue.Type.VULNERABILITY;
    }
  }

  Optional<Set<ImmutableSonarLocation>> mapSecondaryLocations(final ImmutableResult result) {
    final Optional<List<ImmutableLocation>> locations = result.locations();
    if (locations.isEmpty() || locations.get().size() < 2) {
      return Optional.empty();
    }
    return Optional.of(locations.get().stream().skip(1).map(location -> mapLocation(location, result.message().get()))
        .collect(Collectors.toSet()));
  }

  Optional<ImmutableSonarLocation> mapPrimaryLocation(final ImmutableResult result) {
    final Optional<List<ImmutableLocation>> locations = result.locations();
    if (locations.isEmpty() || locations.get().isEmpty()) {
      return Optional.empty();
    }
    ImmutableLocation l = locations.get().get(0);
    return Optional.of(mapLocation(l, result.message().orElse("REALLY?")));
  }

  private ImmutableSonarLocation mapLocation(final ImmutableLocation location, final String message) {

    final List<String> srcDirPom = List.of("src/");

    Optional<String> st = srcDirPom.stream()
        .map(s -> s.split("/")[0] + "/") // consider only first folder (e.g., src/) in order to capture generated
                                         // folders also
        // if filepath contains dir but does not start with it, it seems to be prefixed
        // by module name
        .filter(srcDirFilter -> !location.uri().startsWith(srcDirFilter) && location.uri().contains(srcDirFilter))
        .findFirst();

    String newPath = location.uri();
    if (st.isPresent()) {
      // remove module name
      newPath = location.uri().substring(location.uri().indexOf("/" + st.get()) + 1);
    }

    return ImmutableSonarLocation.builder()
        .filePath(newPath)
        .message(message)
        .textRange(mapTextRange(location.region().get()))
        .build();
  }

  private ImmutableTextRange mapTextRange(final ImmutableRegion region) {
    if (region == null) {
      return null;
    }
    return ImmutableTextRange.builder()
        .startLine(region.startLine())
        .endLine(region.startLine())
        .startColumn(region.startColumn())
        .endColumn(region.endColumn())
        .build();
  }

  public String getSummary() {
    return String.format("parsed %d Rules, %d Results resulting in %d issues.",
        rules.size(), results.size(), mappedIssues.getResult().size());
  }

  public Issues getMappedIssues(final String[] patternsToExclude) {
    return patternsToExclude != null && patternsToExclude.length > 0
        ? mappedIssues.applyFilter(issue -> !isMatchingExlusionPattern(issue, patternsToExclude))
        : mappedIssues;
  }

  private boolean isMatchingExlusionPattern(final ImmutableIssue issue, final String[] patternsToExclude) {
    final Optional<ImmutableSonarLocation> primaryLocation = issue.primaryLocation();
    if (primaryLocation.isEmpty()) {
      return false;
    }
    final String filePath = primaryLocation.get().filePath();
    return Arrays.stream(patternsToExclude)
        .anyMatch(
            pattern -> Pattern.compile(".*" + pattern + ".*", Pattern.CASE_INSENSITIVE).matcher(filePath).matches());
  }

}
