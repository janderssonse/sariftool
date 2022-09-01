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
public class Region {

  @Builder.Default
  private Integer startLine = 0;
  @Builder.Default
  private Integer startColumn = 0;
  @Builder.Default
  private Integer endColumn = 0;

  @Override
  public String toString() {
    return String.format("Line %d, Column %d%s", startLine, startColumn, endColumn != null && endColumn > 0 ? ":" + endColumn : "");
  }
}
