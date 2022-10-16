// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sariftool.mapper;

import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

import se.janderssonse.sariftool.model.sarif.Driver;
import se.janderssonse.sariftool.model.sarif.Result;
import se.janderssonse.sariftool.model.sarif.Rule;

public final class LogMapper implements Mapper {

 private static final Logger LOG = Logger.getLogger(LogMapper.class.getName());

  @Override
  public void onFinding(final Result result) {
    LOG.fine(result.toString());
  }

  @Override
  public void onVersion(final String version) {
    LOG.fine("Sarif version: " + version);
  }

  @Override
  public void onSchema(final String schema) {
    LOG.fine("Sarif schema: " + schema);
  }

  @Override
  public void onDriver(final Driver driver) {
    LOG.fine("Driver: " + driver);
  }

  @Override
  public void onRule(final Rule rule) {
    LOG.fine(String.format("Processed rule[%s]: %s", rule.id(), rule.name()));
  }

  @Override
  public String summary() {
    return null;
  }

  @Override
  public void writeResult(final Path sarifFile, final Path targetPath, final List<String> excludePaths) {
  }
}
