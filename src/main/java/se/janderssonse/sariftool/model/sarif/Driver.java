// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0
package se.janderssonse.sariftool.model.sarif;

import java.util.Optional;

public record Driver(
        Optional<String> name,
        Optional<String> organization,
        Optional<String> semanticVersion) {

    public String asShortString() {
        return String.format("%s %s v%s", organization.orElse(""), name.orElse(""), semanticVersion.orElse(""));
    }

    @Override
    public final String toString() {
        return String.format("%s[%nname=%s%norganization=%s%nsemanticVersion=%s%n]",
                this.getClass().getName(),
                name().orElse(""),
                organization().orElse(""),
                semanticVersion().orElse(""));
    }
}
