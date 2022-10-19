// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sariftool.model.sarif;

import java.util.Optional;

public record Location(String uri, Optional<String> uriBaseId, Optional<Integer> index, Optional<Region> region) {

    @Override
    public final String toString() {
        return String.format("%s[%nuri=%s%nuriBaseId=%s%nindex=%s%nregion%n]",
                this.getClass().getName(),
                uri != null ? uri : "",
                uriBaseId.isPresent() ? uriBaseId.get() : "",
                index.isPresent() ? index.get() : "",
                region.isPresent() ? region.get() : "");
    }
}
