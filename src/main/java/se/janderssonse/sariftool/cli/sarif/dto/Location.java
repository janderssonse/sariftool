// SPDX-FileCopyrightText: 2021 Baloise Group
// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sariftool.cli.sarif.dto;

import java.util.Optional;

import org.immutables.value.Value;


@Value.Immutable
public abstract class Location {

  /**
   * The default value for uri.
   * @return An uri string, default empty
   */
  @Value.Default
  public String uri() {
    return "";
  }
  public abstract Optional<String> uriBaseId();
  public abstract Optional<Integer> index();
  public abstract Optional<ImmutableRegion> region();

  @Override
  public final String toString() {
    return String.format("%s, %s", !uri().isEmpty() ? uri() : "<URI_MISSING>",
        region().isPresent() ? region().get().toString() : "n/a");
  }
}
