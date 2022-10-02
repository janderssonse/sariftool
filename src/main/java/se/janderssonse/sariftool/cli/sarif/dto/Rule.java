// SPDX-FileCopyrightText: 2021 Baloise Group
// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sariftool.cli.sarif.dto;

import java.util.Optional;

import org.immutables.value.Value;

@Value.Immutable
public abstract class Rule {
  public abstract Optional<String> id();

  public abstract Optional<String> name();

  public abstract Optional<String> shortDescription();

  public abstract Optional<String> fullDescription();

  public abstract Optional<Level> level();

  public abstract Optional<ImmutableRuleProperties> properties();

  @Override
  public final String toString() {
    return String.format("Rule[%s]%s: '%s'; %s", id().orElse("N/A"), level().isPresent() ? "-" + level().get() : "",
        name().orElse("N/A"), properties().isPresent() ? properties().get() : "N/A");
  }

  public enum Level {
    /**
     * The rule specified by ruleId was evaluated and a problem was found.
     */
    WARNING,
    /**
     * The rule specified by ruleId was evaluated and a serious problem was found.
     */
    ERROR,
    /**
     * The rule specified by ruleId was evaluated and a minor problem or an
     * opportunity to improve the code was found.
     */
    NOTE,
    /**
     * The concept of “severity” does not apply to this result because the kind
     * property (§3.27.9) has a value other than "fail".
     */
    NONE
  }
}
