// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sariftool.model.sarif;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public record Result(Optional<String> ruleId,
        Optional<Integer> ruleIndex,
        Optional<String> message,
        Optional<List<Location>> locations) {

    @Override
    public final String toString() {
        return String.format("%s[%nruleId=%s%nruleIndex=%s%nmessage=%s%nlocations=%s%n]",
                this.getClass().getName(),
                ruleId.isPresent() ? ruleId.get() : "",
                ruleIndex.isPresent() ? ruleIndex.get() : "",
                message.isPresent() ? message.get() : "",
                locations.isPresent()
                        ? locations.get().stream().map(it -> it.toString()).collect(Collectors.joining(","))
                        : "");
    }
}
