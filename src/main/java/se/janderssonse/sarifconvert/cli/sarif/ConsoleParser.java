// SPDX-FileCopyrightText: 2021 Baloise Group
// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sarifconvert.cli.sarif;


import java.util.logging.Logger;

import se.janderssonse.sarifconvert.cli.sarif.dto.Driver;
import se.janderssonse.sarifconvert.cli.sarif.dto.ImmutableDriver;
import se.janderssonse.sarifconvert.cli.sarif.dto.ImmutableResult;
import se.janderssonse.sarifconvert.cli.sarif.dto.ImmutableRule;

public class ConsoleParser implements ParserCallback {

  static Logger logger = Logger.getLogger(ConsoleParser.class.getName());

  @Override
  public void onFinding(ImmutableResult result) {
    logger.fine(result.toString());
  }

  @Override
  public void onVersion(String version) {
    logger.fine("Sarif version: " + version);
  }

  @Override
  public void onSchema(String schema) {
    logger.fine("Sarif schema: " + schema);
  }

  @Override
  public void onDriver(ImmutableDriver driver) {
    logger.fine("Driver: " + driver);
  }

  @Override
  public void onRule(ImmutableRule rule) {
    logger.fine(String.format("Processed rule[%s]: %s", rule.id(), rule.name() ));
  }
}
