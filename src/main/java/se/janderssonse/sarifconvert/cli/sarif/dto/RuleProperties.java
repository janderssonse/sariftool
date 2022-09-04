// SPDX-FileCopyrightText: 2021 Baloise Group
// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sarifconvert.cli.sarif.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

@Value.Immutable
public abstract class RuleProperties {

  public abstract Optional<String> id();
  public abstract Optional<String> name();
  public abstract Optional<String> description();
  public abstract Optional<ArrayList<String>> tags();
  public abstract Optional<String> kind();
  public abstract Optional<String> precision();
  public abstract Optional<Severity> severity();

  @Override
  public String toString() {
    return "RuleProperties{" +
            "id='" + id().orElse("") + '\'' +
            ", tags=" + tags().orElse(new ArrayList<>(List.of(""))) +
            ", kind='" + kind().orElse("") + '\'' +
            ", precision='" + precision().orElse("") + '\'' +
            ", severity=" + severity().orElse(null) +
            '}';
  }

  public enum Severity {
    warning,
    error,
    recommendation
  }
}
