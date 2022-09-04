// SPDX-FileCopyrightText: 2021 Baloise Group
// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sarifconvert.cli.sarif;

import se.janderssonse.sarifconvert.cli.sarif.dto.Driver;
import se.janderssonse.sarifconvert.cli.sarif.dto.ImmutableDriver;
import se.janderssonse.sarifconvert.cli.sarif.dto.ImmutableResult;
import se.janderssonse.sarifconvert.cli.sarif.dto.ImmutableRule;
import se.janderssonse.sarifconvert.cli.sarif.dto.Rule;

public interface ParserCallback {

  void onFinding(ImmutableResult result);

  void onVersion(String version);

  void onSchema(String schema);

  void onDriver(ImmutableDriver driver);

  void onRule(ImmutableRule rule);
}
