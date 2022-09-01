// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sariftool.model.sonar;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Issues(@JsonProperty(value = "issues") List<Issue> result) {
    @Override
    public String toString() {
        return "Issues []" + result.stream().map(it -> it.toString()).collect(Collectors.joining(","));
    }

    public Issues applyFilter(final Predicate<Issue> predicate) {
        return new Issues(result().stream().filter(predicate).toList());
    }
}
