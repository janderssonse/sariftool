// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sariftool.model.sonar;

import java.util.Optional;

public record TextRange(
        Integer startLine,
        Integer endLine,
        Optional<Integer> startColumn,
        Optional<Integer> endColumn) {
}
