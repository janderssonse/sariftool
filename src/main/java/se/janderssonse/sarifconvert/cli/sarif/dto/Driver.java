// SPDX-FileCopyrightText: 2021 Baloise Group
// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sarifconvert.cli.sarif.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@Builder(toBuilder = true)
public class Driver {

  private String name;
  private String organization;
  private String semanticVersion;

  @Override
  public String toString() {
    return String.format("%s %s %s",
            organization==null ? "n/a" : organization,
            name == null ? "n/a" : name,
            StringUtils.isBlank(semanticVersion) ? "" : "v" + semanticVersion).trim();
  }
}
