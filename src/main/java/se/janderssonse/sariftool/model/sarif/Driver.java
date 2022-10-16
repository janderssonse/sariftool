// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sariftool.model.sarif;

import java.util.Optional;

public record Driver(Optional<String> name,
        Optional<String> organization,
        Optional<String> semanticVersion) {

    @Override
    public final String toString() {
        return String.format("%s %s %s",
                organization().isEmpty() ? "n/a" : organization().get(),
                name().isEmpty() ? "n/a" : name().get(),
                semanticVersion().isEmpty() ? "" : "v" + semanticVersion().get()).trim();
    }
}
