// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sariftool.model.sarif;

import java.util.Optional;

public record Region(Integer startLine, Optional<Integer> startColumn, Optional<Integer> endColumn) {

    @Override
    public final String toString() {
        return String.format("%s[%nstartLine=%s%startColumn=%s%nendColumn%n]",
                this.getClass().getName(),
                startLine != null ? startLine : "",
                startColumn.isPresent() ? startColumn.get() : "",
                endColumn.isPresent() ? endColumn.get() : "");
    }
}
