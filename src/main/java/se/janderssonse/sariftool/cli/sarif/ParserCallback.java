// SPDX-FileCopyrightText: 2021 Baloise Group
// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sariftool.cli.sarif;

import se.janderssonse.sariftool.cli.sarif.dto.ImmutableDriver;
import se.janderssonse.sariftool.cli.sarif.dto.ImmutableResult;
import se.janderssonse.sariftool.cli.sarif.dto.ImmutableRule;

public interface ParserCallback {

  void onFinding(ImmutableResult result);

  void onVersion(String version);

  void onSchema(String schema);

  void onDriver(ImmutableDriver driver);

  void onRule(ImmutableRule rule);
}
