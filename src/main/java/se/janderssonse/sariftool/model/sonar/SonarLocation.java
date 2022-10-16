// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sariftool.model.sonar;

public record SonarLocation(

    String message,
    String filePath,
    TextRange textRange) {

}
