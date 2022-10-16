// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sariftool.model.sarif;

import java.util.ArrayList;
import java.util.Optional;

public record RuleProperties(
        Optional<String> id,
        Optional<String> name,
        Optional<String> description,
        Optional<ArrayList<String>> tags,
        Optional<String> kind,
        Optional<String> precision,
        Optional<Severity> severity) {

    public enum Severity {
        warning,
        error,
        recommendation
    }
}
