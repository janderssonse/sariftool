// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sariftool.mapper;

import java.nio.file.Path;
import java.util.List;

public interface Mapper {

    void map(Path targetPath, List<String> excludePaths);

    enum MapperType {
        SONAR,
    }
}
