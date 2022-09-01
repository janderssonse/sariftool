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
public class Location {

  private String uri;
  private String uriBaseId;
  private Integer index;
  private Region region;

  @Override
  public String toString() {
    return String.format("%s, %s"
            , uri != null ? uri : "<URI_MISSING>"
            , region != null ? region.toString() : "n/a");
  }
}
