// SPDX-FileCopyrightText: 2021 Baloise Group
// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sariftool.mapper;

import java.nio.file.Path;
import java.util.List;

import se.janderssonse.sariftool.model.sarif.Driver;
import se.janderssonse.sariftool.model.sarif.Result;
import se.janderssonse.sariftool.model.sarif.Rule;

public interface Mapper {

    void onFinding(Result result);

    void onVersion(String version);

    void onSchema(String schema);

    void onDriver(Driver driver);

    void onRule(Rule rule);

    String summary();

    void writeResult(Path sarifFile, Path targetPath, List<String> excludePaths);

    enum MapperType {
        SONAR,
        LOG;

    }
}
