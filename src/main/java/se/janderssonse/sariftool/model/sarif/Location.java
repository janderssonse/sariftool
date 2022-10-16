// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sariftool.model.sarif;

import java.util.Optional;

public record Location(String uri, Optional<String> uriBaseId, Optional<Integer> index, Optional<Region> region) {

  public Location(final Optional<String> uriBaseId, final Optional<Integer> index, final Optional<Region> region) {
    this("", uriBaseId, index, region);
  }

  @Override
  public final String toString() {
    return String.format("%s, %s", !uri().isEmpty() ? uri() : "<URI_MISSING>",
        region().isPresent() ? region().get().toString() : "n/a");
  }
}
