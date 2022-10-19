// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sariftool.model.sarif;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

public record RuleProperties(
        Optional<String> id,
        Optional<String> name,
        Optional<String> description,
        Optional<ArrayList<String>> tags,
        Optional<String> kind,
        Optional<String> precision,
        Optional<Severity> severity) {

    @Override
    public final String toString() {
        return String.format(
                "%s[%nid=%s%nname=%s%ndescription=%s%ntags%nlevel=%s%nkind=%s%nprecision=%s%nseverity=%s%n]",
                this.getClass().getName(),
                id.isPresent() ? id.get() : "",
                name.isPresent() ? name.get() : "",
                description.isPresent() ? description.get() : "",
                tags.isPresent() ? tags.get().stream().map(it -> it.toString()).collect(Collectors.joining(",")) : "",
                kind.isPresent() ? kind.get() : "",
                precision.isPresent() ? precision.get() : "",
                severity.isPresent() ? severity.get() : "");
    }

    public enum Severity {
        warning,
        error,
        recommendation
    }
}
