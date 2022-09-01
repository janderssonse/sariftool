// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sariftool.model.sonar;

import java.util.Optional;
import java.util.Set;

import se.janderssonse.sariftool.model.sarif.Rule;
import se.janderssonse.sariftool.model.sarif.RuleProperties;

public record Issue(
        Optional<String> engineId,
        Optional<String> ruleId,
        Optional<Severity> severity,
        Optional<Type> type,
        Optional<SonarLocation> primaryLocation,
        Optional<Set<SonarLocation>> secondaryLocations,
        Optional<Integer> effortMinutes) {

    @Override
    public final String toString() {
        return String.format(
                "%s[%nengineId=%s%nruleId=%s%nseverity=%s%ntype%nprimaryLocation=%s%nsecondaryLocation=%s%neffortMinutes=%s%n]",
                this.getClass().getName(),
                engineId.isPresent() ? engineId.get() : "",
                ruleId.isPresent() ? ruleId.get() : "",
                severity.isPresent() ? severity.get() : "",
                type.isPresent() ? type.get() : "",
                primaryLocation.isPresent() ? primaryLocation.get() : "",
                secondaryLocations.isPresent() ? secondaryLocations.get() : "",
                effortMinutes.isPresent() ? effortMinutes.get() : "");
    }

    public enum Severity {
        BLOCKER,
        CRITICAL,
        MAJOR,
        MINOR,
        INFO
    }

    public enum Type {
        BUG,
        VULNERABILITY,
        CODE_SMELL
    }

    public static Optional<Issue.Type> severityToType(final Optional<Issue.Severity> severity) {
        if (severity.isEmpty()) {
            return Optional.empty();
        }

        return switch (severity.get()) {
            case INFO, MINOR, MAJOR -> Optional.of(Issue.Type.CODE_SMELL);
            case BLOCKER -> Optional.of(Issue.Type.BUG);
            case CRITICAL -> Optional.of(Issue.Type.VULNERABILITY);
        };
    }

    public static Optional<Issue.Severity> toSeverityBasedOnRulePropSeverity(final Optional<Rule.Level> level,
            final RuleProperties properties) {

        final RuleProperties.Severity ruleSeverity = properties.severity().get();
        final Optional<String> rulePrecision = properties.precision();

        Optional<Issue.Severity> severity = switch (ruleSeverity) {
            case recommendation -> Optional.of(Issue.Severity.INFO);
            case warning -> toSeverityBasedOnRulePrecisionWarning(rulePrecision);
            case error -> toSeverityBasedOnRulePrecisionError(rulePrecision);
            default -> Optional.empty();
        };

        if (severity.isEmpty()) {
            severity = toSeverityBasedOnRuleLevel(level);
        }
        return severity;
    }

    private static Optional<Issue.Severity> toSeverityBasedOnRulePrecisionWarning(
            final Optional<String> rulePrecision) {

        return switch (rulePrecision.orElse("")) {
            case "medium" -> Optional.of(Issue.Severity.MINOR);
            case "high" -> Optional.of(Issue.Severity.MAJOR);
            case "very-high" -> Optional.of(Issue.Severity.CRITICAL);
            default -> Optional.empty();
        };
    }

    private static Optional<Issue.Severity> toSeverityBasedOnRulePrecisionError(
            final Optional<String> rulePrecision) {

        return switch (rulePrecision.orElse("")) {
            case "medium", "high" -> Optional.of(Issue.Severity.CRITICAL);
            case "very-high" -> Optional.of(Issue.Severity.BLOCKER);
            default -> Optional.empty();
        };
    }

    private static Optional<Issue.Severity> toSeverityBasedOnRuleLevel(final Optional<Rule.Level> level) {
        if (level.isPresent()) {
            return switch (level.get()) {
                case NONE, NOTE -> Optional.of(Issue.Severity.MINOR);
                case WARNING -> Optional.of(Issue.Severity.MAJOR);
                case ERROR -> Optional.of(Issue.Severity.CRITICAL);
                default -> Optional.empty();
            };
        }

        return Optional.empty();
    }
}
