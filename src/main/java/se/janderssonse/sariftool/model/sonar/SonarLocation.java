// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sariftool.model.sonar;

public record SonarLocation(
        String message,
        String filePath,
        TextRange textRange) {

    @Override
    public final String toString() {
        return String.format(
                "%s[%nmessage=%s%nfilePath=%s%ntextRange=%s%n%n]",
                this.getClass().getName(),
                message != null ? message : "",
                filePath != null ? filePath : "",
                textRange != null ? textRange : "");
    }
}
