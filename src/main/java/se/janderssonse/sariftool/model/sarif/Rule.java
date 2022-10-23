// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sariftool.model.sarif;

import java.util.Optional;

public record Rule(
        Optional<String> id,
        Optional<String> name,
        Optional<String> shortDescription,
        Optional<String> fullDescription,
        Optional<Level> level,
        Optional<RuleProperties> properties) {

    @Override
    public final String toString() {
        return String.format("%s[%nid=%s%nname=%s%nshortDescription=%s%nfullDescription%nlevel=%s%nproperties=%s%n]",
                this.getClass().getName(),
                id.isPresent() ? id.get() : "",
                name.isPresent() ? name.get() : "",
                shortDescription.isPresent() ? shortDescription.get() : "",
                fullDescription.isPresent() ? fullDescription.get() : "",
                level.isPresent() ? level.get() : "",
                properties.isPresent() ? properties.get() : "");
    }

    public enum Level {
        WARNING,
        ERROR,
        NOTE,
        NONE
    }


}
