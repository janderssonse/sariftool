// SPDX-FileCopyrightText: 2021 Baloise Group
// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sarifconvert.cli.sarif.dto;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.immutables.value.Value;

@Value.Immutable
public abstract class Driver {

  public abstract Optional<String> name();
  public abstract Optional<String> organization();
  public abstract Optional<String> semanticVersion();

  @Override
  public String toString() {
    return String.format("%s %s %s",
            organization().isEmpty() ? "n/a" : organization().get(),
            name().isEmpty() ? "n/a" : name().get(),
            StringUtils.isBlank(semanticVersion().orElse("")) ? "" : "v" + semanticVersion().get()).trim();
  }
}
