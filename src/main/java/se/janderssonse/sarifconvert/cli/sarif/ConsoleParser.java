/*
 Copyright 2021 Baloise Group

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package se.janderssonse.sarifconvert.cli.sarif;


import java.util.logging.Logger;

import se.janderssonse.sarifconvert.cli.sarif.dto.Driver;
import se.janderssonse.sarifconvert.cli.sarif.dto.Result;
import se.janderssonse.sarifconvert.cli.sarif.dto.Rule;

public class ConsoleParser implements ParserCallback {

  static Logger logger = Logger.getLogger(ConsoleParser.class.getName());

  @Override
  public void onFinding(Result result) {
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
  public void onDriver(Driver driver) {
    logger.fine("Driver: " + driver);
  }

  @Override
  public void onRule(Rule rule) {
    logger.fine(String.format("Processed rule[%s]: %s", rule.getId(), rule.getName() ));
  }
}
