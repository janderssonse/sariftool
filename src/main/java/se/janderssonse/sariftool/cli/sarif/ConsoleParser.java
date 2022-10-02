// SPDX-FileCopyrightText: 2021 Baloise Group
// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sariftool.cli.sarif;

import java.util.logging.Logger;

import se.janderssonse.sariftool.cli.sarif.dto.ImmutableDriver;
import se.janderssonse.sariftool.cli.sarif.dto.ImmutableResult;
import se.janderssonse.sariftool.cli.sarif.dto.ImmutableRule;

public final class ConsoleParser implements ParserCallback {

 private static Logger logger = Logger.getLogger(ConsoleParser.class.getName());

  @Override
  public void onFinding(final ImmutableResult result) {
    logger.fine(result.toString());
  }

  @Override
  public void onVersion(final String version) {
    logger.fine("Sarif version: " + version);
  }

  @Override
  public void onSchema(final String schema) {
    logger.fine("Sarif schema: " + schema);
  }

  @Override
  public void onDriver(final ImmutableDriver driver) {
    logger.fine("Driver: " + driver);
  }

  @Override
  public void onRule(final ImmutableRule rule) {
    logger.fine(String.format("Processed rule[%s]: %s", rule.id(), rule.name()));
  }
}
