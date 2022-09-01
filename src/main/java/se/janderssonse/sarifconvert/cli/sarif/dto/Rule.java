// SPDX-FileCopyrightText: 2021 Baloise Group
// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sarifconvert.cli.sarif.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder(toBuilder = true)
public class Rule {
  private String id;
  private String name;
  private String shortDescription;
  private String fullDescription;
  private Level level;
  private RuleProperties properties;

  @Override
  public String toString() {
    return String.format("Rule[%s]%s: '%s'; %s"
            , id
            , level != null ? "-" + level : ""
            , name
            , properties);
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
     * The rule specified by ruleId was evaluated and a minor problem or an opportunity to improve the code was found.
     */
    NOTE,
    /**
     * The concept of “severity” does not apply to this result because the kind property (§3.27.9) has a value other than "fail".
     */
    NONE
  }
}
