// SPDX-FileCopyrightText: 2021 Baloise Group
// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sariftool.mapper;

import se.janderssonse.sariftool.SarifParser;
import se.janderssonse.sariftool.model.sarif.Location;
import se.janderssonse.sariftool.model.sarif.Region;
import se.janderssonse.sariftool.model.sarif.Result;
import se.janderssonse.sariftool.model.sarif.Rule;
import se.janderssonse.sariftool.model.sonar.Issue;
import se.janderssonse.sariftool.model.sonar.Issues;
import se.janderssonse.sariftool.model.sonar.SonarLocation;
import se.janderssonse.sariftool.model.sonar.TextRange;
import se.janderssonse.sariftool.util.JsonWrapper;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public record SonarMapper(SarifParser parser) implements Mapper {

    private static final Logger LOG = Logger.getLogger(SonarMapper.class.getName());

    private Issue toIssue(final Result result) {
        final Optional<Issue.Severity> severity = toIssueSeverity(result.ruleId().get());
        String driverName = parser.getDriver().isPresent() ? parser.getDriver().get().asShortString() : "";
        return new Issue(Optional.of(driverName),
                Optional.of(result.ruleId().get()),
                severity,
                Issue.severityToType(severity),
                toPrimaryLocation(result),
                toSecondaryLocations(result),
                Optional.of(0));
    }

    private Optional<Issue.Severity> toIssueSeverity(final String ruleId) {

        final Optional<Rule> matchingRule = parser.getRules().stream()
                .filter(rule -> rule.id().get().equals(ruleId))
                .findFirst();

        if (matchingRule.isPresent() && matchingRule.get().properties().isPresent()
                && matchingRule.get().properties().get().severity().isPresent()) {
            return Issue.toSeverityBasedOnRulePropSeverity(matchingRule.get().level(), matchingRule.get().properties().get());
        }
        return Optional.of(Issue.Severity.INFO);
    }

    private Optional<Set<SonarLocation>> toSecondaryLocations(final Result result) {
        final Optional<List<Location>> locations = result.locations();
        if (locations.isEmpty() || locations.get().size() < 2) {
            return Optional.empty();
        }
        return Optional
                .of(locations.get().stream().skip(1).map(location -> toLocation(location, result.message().get()))
                        .collect(Collectors.toSet()));
    }

    private Optional<SonarLocation> toPrimaryLocation(final Result result) {
        final Optional<List<Location>> locations = result.locations();
        if (locations.isEmpty() || locations.get().isEmpty()) {
            return Optional.empty();
        }
        Location l = locations.get().get(0);
        return Optional.of(toLocation(l, result.message().orElse("REALLY?")));
    }

    private SonarLocation toLocation(final Location location, final String message) {
        return new SonarLocation(message, location.uri(), toTextRange(location.region().get()));
    }

    private TextRange toTextRange(final Region region) {
        if (region == null) {
            return null;
        }
        return new TextRange(
                region.startLine(),
                region.endLine(),
                region.startColumn(),
                region.endColumn());
    }

    private Issues filterIssues(final String[] patternsToExclude, final Issues issues) {
        return patternsToExclude != null && patternsToExclude.length > 0
                ? issues.applyFilter(issue -> !isMatchingExlusionPattern(issue, patternsToExclude))
                : issues;
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

    public void map(final Path targetPath, final List<String> excludePaths) {

        Issues issues = new Issues(parser.getResults().stream()
                .map(result -> toIssue(result))
                .toList());

        Issues filteredIssues = filterIssues(excludePaths.toArray(new String[0]), issues);

        JsonWrapper.toFile(targetPath, filteredIssues);

        LOG.info(String.format("Wrote file: target '%s', issues '%s', excluded paths: '%s'", targetPath,
                filteredIssues.result().size(),
                excludePaths.stream().collect(Collectors.joining(","))));
    }
}
