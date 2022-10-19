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

    /*
     * public final String asShortString() {
     * String result = String.format("Found issue based on rule '%s': '%s'",
     * ruleId().orElse(""),
     * message().orElse(""));
     * if (locations().isPresent() && !locations().get().isEmpty()) {
     * result += "\nin '" + locations().get().get(0).toString() + "'";
     * }
     * return result;
     * }
     */

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
