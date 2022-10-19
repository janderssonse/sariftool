// SPDX-FileCopyrightText: 2021 Baloise Group
// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sariftool.mapper;

import se.janderssonse.sariftool.model.sarif.Driver;
import se.janderssonse.sariftool.model.sarif.Location;
import se.janderssonse.sariftool.model.sarif.Region;
import se.janderssonse.sariftool.model.sarif.Result;
import se.janderssonse.sariftool.model.sarif.Rule;
import se.janderssonse.sariftool.model.sarif.RuleProperties;
import se.janderssonse.sariftool.model.sonar.Issue;
import se.janderssonse.sariftool.model.sonar.Issues;
import se.janderssonse.sariftool.model.sonar.SonarLocation;
import se.janderssonse.sariftool.model.sonar.TextRange;
import se.janderssonse.sariftool.util.JsonWrapper;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class SonarMapper implements Mapper {

    private static final Logger LOG = Logger.getLogger(SonarMapper.class.getName());

    private final List<Result> results = new ArrayList<>();
    private List<Rule> rules = new ArrayList<>();
    private final Issues mappedIssues = new Issues();
    private Driver driver;

    public void setRules(final List<Rule> rules) {
        this.rules = rules;
    }

    public void setDriver(final Driver driver) {
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
    public void onFinding(final Result result) {
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
    public void onDriver(final Driver driver) {
        this.driver = driver;
    }

    @Override
    public void onRule(final Rule rule) {
        rules.add(rule);
    }

    public void finding(final Result result) {
        this.onFinding(result);
    }

    private Issue mapResult(final Result result) {
        final Issue.Severity severity = mapSeverity(result.ruleId().get());
        String driverName = driver != null ? driver.asShortString() : "";
        return new Issue(Optional.of(driverName),
                Optional.of(result.ruleId().get()),
                Optional.of(severity),
                Optional.of(mapType(severity)),
                mapPrimaryLocation(result),
                mapSecondaryLocations(result),
                Optional.of(0));
    }

    Issue.Severity mapSeverity(final String ruleId) {
        final Rule matchingRule = rules.stream().filter(rule -> rule.id().get().equals(ruleId)).findFirst()
                .orElse(null);
        if (matchingRule != null && matchingRule.properties().isPresent()
                && matchingRule.properties().get().severity().isPresent()) {
            return mapRuleToIssueSeverity(matchingRule.level().orElse(null), matchingRule.properties());
        }
        return Issue.Severity.INFO;
    }

    Issue.Severity mapRuleToIssueSeverity(final Rule.Level level,
            final Optional<RuleProperties> properties) {
        if (properties.isEmpty() || properties.get().severity().isEmpty()) {
            // without properties the only basis to map severity is the rule level.
            return (level == null) ? null : mapRuleLevelToSeverity(level);
        }

        final RuleProperties.Severity ruleSeverity = properties.get().severity().get();
        final Optional<String> rulePrecision = properties.get().precision();

        switch (ruleSeverity) {
            case recommendation:
                return Issue.Severity.INFO;
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

    private Issue.Severity mapRuleSeverityError(final Rule.Level level,
            final Optional<String> rulePrecision) {
        if (rulePrecision.isPresent()) {
            switch (rulePrecision.get().toLowerCase()) {
                case "medium":
                case "high":
                    return Issue.Severity.CRITICAL;
                case "very-high":
                    return Issue.Severity.BLOCKER;
                default:
                    // not decisive yet
            }
        }
        // if not set or unknown consider level as second criteria
        if (level == Rule.Level.ERROR) {
            return Issue.Severity.BLOCKER;
        }
        return Issue.Severity.CRITICAL;
    }

    private Issue.Severity mapRuleSeverityWarning(final Rule.Level level,
            final Optional<String> rulePrecision) {
        if (rulePrecision.isPresent()) {
            switch (rulePrecision.get().toLowerCase()) {
                case "medium":
                    return Issue.Severity.MINOR;
                case "high":
                    return Issue.Severity.MAJOR;
                case "very-high":
                    return Issue.Severity.CRITICAL;
                default:
                    // not decisive yet
            }
        }
        // if not set or unknown consider level as second criteria
        return (level == null) ? Issue.Severity.MINOR : mapRuleLevelToSeverity(level);
    }

    private Issue.Severity mapRuleLevelToSeverity(final Rule.Level level) {
        switch (level) {
            case NONE:
            case NOTE:
                return Issue.Severity.MINOR;
            case WARNING:
                return Issue.Severity.MAJOR;
            case ERROR:
                return Issue.Severity.CRITICAL;
            default:
                return null;
        }
    }

    public Issue.Type mapType(final Issue.Severity severity) {
        if (severity == null) {
            return null;
        }
        switch (severity) {
            case INFO:
            case MINOR:
            case MAJOR:
                return Issue.Type.CODE_SMELL;
            case BLOCKER:
                return Issue.Type.BUG;
            case CRITICAL:
            default:
                return Issue.Type.VULNERABILITY;
        }
    }

    Optional<Set<SonarLocation>> mapSecondaryLocations(final Result result) {
        final Optional<List<Location>> locations = result.locations();
        if (locations.isEmpty() || locations.get().size() < 2) {
            return Optional.empty();
        }
        return Optional
                .of(locations.get().stream().skip(1).map(location -> mapLocation(location, result.message().get()))
                        .collect(Collectors.toSet()));
    }

    Optional<SonarLocation> mapPrimaryLocation(final Result result) {
        final Optional<List<Location>> locations = result.locations();
        if (locations.isEmpty() || locations.get().isEmpty()) {
            return Optional.empty();
        }
        Location l = locations.get().get(0);
        return Optional.of(mapLocation(l, result.message().orElse("REALLY?")));
    }

    private SonarLocation mapLocation(final Location location, final String message) {

        final List<String> srcDirPom = List.of("src/");

        Optional<String> st = srcDirPom.stream()
                .map(s -> s.split("/")[0] + "/") // consider only first folder (e.g., src/) in order to capture
                                                 // generated
                                                 // folders also
                // if filepath contains dir but does not start with it, it seems to be prefixed
                // by module name
                .filter(srcDirFilter -> !location.uri().startsWith(srcDirFilter)
                        && location.uri().contains(srcDirFilter))
                .findFirst();

        String newPath = location.uri();
        if (st.isPresent()) {
            // remove module name
            newPath = location.uri().substring(location.uri().indexOf("/" + st.get()) + 1);
        }

        return new SonarLocation(message, newPath, mapTextRange(location.region().get()));
    }

    private TextRange mapTextRange(final Region region) {
        if (region == null) {
            return null;
        }
        return new TextRange(
                region.startLine(),
                region.startLine(), // TO-DO, seems bug
                region.startColumn(),
                region.endColumn());
    }

    public String summary() {
        return String.format("Parsed %d rules, %d results resulting in %d issues.",
                rules.size(), results.size(), mappedIssues.getResult().size());
    }

    public Issues getMappedIssues(final String[] patternsToExclude) {
        return patternsToExclude != null && patternsToExclude.length > 0
                ? mappedIssues.applyFilter(issue -> !isMatchingExlusionPattern(issue, patternsToExclude))
                : mappedIssues;
    }

    private boolean isMatchingExlusionPattern(final Issue issue, final String[] patternsToExclude) {
        final Optional<SonarLocation> primaryLocation = issue.primaryLocation();
        if (primaryLocation.isEmpty()) {
            return false;
        }
        final String filePath = primaryLocation.get().filePath();
        return Arrays.stream(patternsToExclude)
                .anyMatch(
                        pattern -> Pattern.compile(".*" + pattern + ".*", Pattern.CASE_INSENSITIVE).matcher(filePath)
                                .matches());
    }

    public void writeResult(final Path sarifFile, final Path targetPath, final List<String> excludePaths) {
        Issues filteredIssues = getMappedIssues(excludePaths.toArray(new String[0]));

        JsonWrapper.toFile(targetPath, filteredIssues);

        LOG.info(String.format("Writing to target '%s' containing %d issues and excluded paths %s.", targetPath,
                filteredIssues.getResult().size(), excludePaths.stream().collect(Collectors.joining(","))));
    }
}
