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
