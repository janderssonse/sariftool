// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sariftool.model.sonar;

import java.util.Optional;

public record TextRange(
        Integer startLine,
        Optional<Integer> endLine,
        Optional<Integer> startColumn,
        Optional<Integer> endColumn) {

    @Override
    public final String toString() {
        return String.format(
                "%s[%nmessage=%s%nfilePath=%s%ntextRange=%s%n%n]",
                this.getClass().getName(),
                startLine != null ? startLine : "",
                endLine.isPresent() ? endLine.get() : "",
                startColumn.isPresent() ? startColumn.get() : "",
                endColumn.isPresent() ? endColumn.get() : "");
    }
}
