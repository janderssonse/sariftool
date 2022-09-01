// SPDX-FileCopyrightText: 2021 Baloise Group
// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sarifconvert.cli.sarif;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import se.janderssonse.sarifconvert.cli.sarif.dto.Driver;
import se.janderssonse.sarifconvert.cli.sarif.dto.Location;
import se.janderssonse.sarifconvert.cli.sarif.dto.Region;
import se.janderssonse.sarifconvert.cli.sarif.dto.Result;
import se.janderssonse.sarifconvert.cli.sarif.dto.Rule;
import se.janderssonse.sarifconvert.cli.sarif.dto.RuleProperties;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class SarifParser {

  static final Logger LOGGER = Logger.getLogger(SarifParser.class.getName());

  static final String ELEMENT_SCHEMA = "$schema";
  static final String ELEMENT_ARTIFACT_LOCATION = "artifactLocation";
  static final String ELEMENT_DEFAULT_CONFIGURATION = "defaultConfiguration";
  static final String ELEMENT_DESCRIPTION = "description";
  static final String ELEMENT_DRIVER = "driver";
  static final String ELEMENT_END_COLUMN = "endColumn";
  static final String ELEMENT_FULL_DESCRIPTION = "fullDescription";
  static final String ELEMENT_ID = "id";
  static final String ELEMENT_INDEX = "index";
  static final String ELEMENT_EXTENSIONS = "extensions";
  static final String ELEMENT_KIND = "kind";
  static final String ELEMENT_LEVEL = "level";
  static final String ELEMENT_LOCATIONS = "locations";
  static final String ELEMENT_MESSAGE = "message";
  static final String ELEMENT_NAME = "name";
  static final String ELEMENT_ORGANIZATION = "organization";
  static final String ELEMENT_PHYSICAL_LOCATION = "physicalLocation";
  static final String ELEMENT_PRECISION = "precision";
  static final String ELEMENT_PROBLEM_SEVERITY = "problem.severity";
  static final String ELEMENT_PROPERTIES = "properties";
  static final String ELEMENT_REGION = "region";
  static final String ELEMENT_RESULTS = "results";
  static final String ELEMENT_RULES = "rules";
  static final String ELEMENT_RULE = "rule";
  static final String ELEMENT_RULE_ID = "ruleId";
  static final String ELEMENT_RULE_INDEX = "ruleIndex";
  static final String ELEMENT_RUNS = "runs";
  static final String ELEMENT_SEMANTIC_VERSION = "semanticVersion";
  static final String ELEMENT_SHORT_DESCRIPTION = "shortDescription";
  static final String ELEMENT_START_COLUMN = "startColumn";
  static final String ELEMENT_START_LINE = "startLine";
  static final String ELEMENT_TAGS = "tags";
  static final String ELEMENT_TEXT = "text";
  static final String ELEMENT_TOOL = "tool";
  static final String ELEMENT_URI = "uri";
  static final String ELEMENT_URI_BASE_ID = "uriBaseId";
  static final String ELEMENT_VERSION = "version";

  private SarifParser() {
    // hide public constructor to not instantiate class
  }

  /**
   * Entry point to parse provided SarifFile. Expected file should be of schema
   * https://raw.githubusercontent.com/oasis-tcs/sarif-spec/master/Schemata/sarif-schema-2.1.0.json
   * and contains at least the runs element. For the result handling multiple
   * callback handler implementing
   * the @{@link ParserCallback} can be provided.
   *
   * @throws FileNotFoundException when sarifInputFile is not present
   */
  public static void execute(File sarifInputFile, ParserCallback... callback) throws IOException,FileNotFoundException {

    try (final FileReader reader = new FileReader(sarifInputFile)) {
      final JsonObject rootObject = JsonParser.parseReader(reader).getAsJsonObject();

      if (rootObject.has(ELEMENT_VERSION)) {
        final String version = getObjectAsStringIfExists(rootObject, SarifParser.ELEMENT_VERSION);
        Arrays.stream(callback).forEach(cb -> cb.onVersion(version));
      }

      if (rootObject.has(ELEMENT_SCHEMA)) {
        final String schema = getObjectAsStringIfExists(rootObject, SarifParser.ELEMENT_SCHEMA);
        Arrays.stream(callback).forEach(cb -> cb.onSchema(schema));
      }

      if (rootObject.has(ELEMENT_RUNS)) {
        for (JsonElement singleRun : rootObject.get(ELEMENT_RUNS).getAsJsonArray()) {
          parseRun(singleRun.getAsJsonObject(), callback);
        }
      }
    }
  }

  private static void parseRun(JsonObject run, ParserCallback[] callback) {
    parseRules(run, callback);
    parseResults(run, callback);
  }

  private static void parseRules(JsonObject run, ParserCallback[] callback) {
    if (run.has(ELEMENT_TOOL)) {
      final JsonObject toolObject = run.get(ELEMENT_TOOL).getAsJsonObject();

      if (toolObject.has(ELEMENT_DRIVER)) {
        final JsonObject driver = toolObject.get(ELEMENT_DRIVER).getAsJsonObject();
        final Driver driverDto = parseDriver(driver);
        Arrays.stream(callback).forEach(cb -> cb.onDriver(driverDto));

        if (driver != null && driver.has(ELEMENT_RULES)) {
          processRules(driver.get(ELEMENT_RULES).getAsJsonArray(), callback);
        }
      }

      if (toolObject.has(ELEMENT_EXTENSIONS)) {
        final JsonArray extensions = toolObject.get(ELEMENT_EXTENSIONS).getAsJsonArray();
        extensions.forEach(extension -> {
          final JsonObject ext = extension.getAsJsonObject();
          if (ext.has(ELEMENT_RULES)) {
            processRules(ext.get(ELEMENT_RULES).getAsJsonArray(), callback);
          }
        });
      }
    }
  }

  private static void processRules(JsonArray rulesArray, ParserCallback[] callback) {
    rulesArray.forEach(rule -> {
      final JsonObject jsonObjectRule = rule.getAsJsonObject();
      final Rule ruleDto = Rule.builder()
          .id(getObjectAsStringIfExists(jsonObjectRule, ELEMENT_ID))
          .name(getObjectAsStringIfExists(jsonObjectRule, ELEMENT_NAME))
          .shortDescription(getTextElement(jsonObjectRule, ELEMENT_SHORT_DESCRIPTION))
          .fullDescription(getTextElement(jsonObjectRule, ELEMENT_FULL_DESCRIPTION))
          .level(parseDefaultConfigLevel(jsonObjectRule))
          .properties(parseRuleProperties(jsonObjectRule))
          .build();
      Arrays.stream(callback).forEach(cb -> cb.onRule(ruleDto));
    });
  }

  private static Driver parseDriver(JsonObject driver) {
    if (driver == null) {
      return null;
    }
    return Driver.builder()
        .name(getObjectAsStringIfExists(driver, ELEMENT_NAME))
        .organization(getObjectAsStringIfExists(driver, ELEMENT_ORGANIZATION))
        .semanticVersion(getObjectAsStringIfExists(driver, ELEMENT_SEMANTIC_VERSION))
        .build();
  }

  private static Rule.Level parseDefaultConfigLevel(JsonObject jsonObjectRule) {
    if (jsonObjectRule.has(ELEMENT_DEFAULT_CONFIGURATION)) {
      final JsonObject defaultConfig = jsonObjectRule.get(ELEMENT_DEFAULT_CONFIGURATION).getAsJsonObject();
      if (defaultConfig.has(ELEMENT_LEVEL)) {
        final String levelAsString = getObjectAsStringIfExists(defaultConfig, ELEMENT_LEVEL);
        try {
          return levelAsString == null ? null : Rule.Level.valueOf(levelAsString.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
          LOGGER.warning(String.format("Failed to interpret %s as Rule.Level: %s", levelAsString, e.getMessage()));
        }
      }
    }
    return null;
  }

  private static RuleProperties parseRuleProperties(JsonObject jsonObjectRule) {
    if (jsonObjectRule.has(ELEMENT_PROPERTIES)) {
      final JsonObject properties = jsonObjectRule.get(ELEMENT_PROPERTIES).getAsJsonObject();
      return RuleProperties.builder()
          .id(getObjectAsStringIfExists(properties, ELEMENT_ID))
          .name(getObjectAsStringIfExists(properties, ELEMENT_NAME))
          .description(getObjectAsStringIfExists(properties, ELEMENT_DESCRIPTION))
          .kind(getObjectAsStringIfExists(properties, ELEMENT_KIND))
          .precision(getObjectAsStringIfExists(properties, ELEMENT_PRECISION))
          .tags(parseTags(properties))
          .severity(parseProblemSeverity(properties))
          .build();
    }
    return null;
  }

  private static String getObjectAsStringIfExists(JsonObject jsonObject, String elementId) {
    return jsonObject != null && jsonObject.has(elementId) ? jsonObject.get(elementId).getAsString() : null;
  }

  private static Integer getObjectAsIntegerIfExists(JsonObject jsonObject, String elementId) {
    return jsonObject != null && jsonObject.has(elementId) ? jsonObject.get(elementId).getAsInt() : null;
  }

  private static RuleProperties.Severity parseProblemSeverity(JsonObject properties) {
    if (properties.has(ELEMENT_PROBLEM_SEVERITY)) {
      final String severityAsString = getObjectAsStringIfExists(properties, ELEMENT_PROBLEM_SEVERITY);
      try {
        return RuleProperties.Severity.valueOf(severityAsString);
      } catch (IllegalArgumentException e) {
        LOGGER.warning(
            String.format("Failed to interpret %s as RuleProperties.Severity: %s", severityAsString, e.getMessage()));
      }
    }
    return null;
  }

  private static String[] parseTags(JsonObject properties) {
    final HashSet<String> result = new HashSet<>();
    if (properties.has(ELEMENT_TAGS)) {
      properties.get(ELEMENT_TAGS).getAsJsonArray().forEach(t -> result.add(t.getAsString()));
    }
    return result.toArray(new String[0]);
  }

  private static void parseResults(JsonObject run, ParserCallback[] callback) {
    if (run.has(ELEMENT_RESULTS)) {
      run.get(ELEMENT_RESULTS).getAsJsonArray().forEach(result -> {
        final JsonObject resultJsonObject = result.getAsJsonObject();
        final Result resultDto = Result.builder()
            .ruleId(getObjectAsStringIfExists(resultJsonObject, ELEMENT_RULE_ID))
            .ruleIndex(getObjectAsIntegerIfExists(resultJsonObject, ELEMENT_RULE_INDEX))
            .message(getTextElement(resultJsonObject, ELEMENT_MESSAGE))
            .locations(parseLocations(resultJsonObject))
            .build();

        if (resultDto.getRuleIndex() == null && resultJsonObject.has(ELEMENT_RULE)) {
          resultDto
              .setRuleIndex(getObjectAsIntegerIfExists(resultJsonObject.getAsJsonObject(ELEMENT_RULE), ELEMENT_INDEX));
        }

        Arrays.stream(callback).forEach(cb -> cb.onFinding(resultDto));
      });
    }
  }

  private static String getTextElement(JsonObject object, String parentProperty) {
    return getObjectAsStringIfExists(object.get(parentProperty).getAsJsonObject(), ELEMENT_TEXT);
  }

  private static List<Location> parseLocations(JsonObject resultJsonObject) {
    final ArrayList<Location> result = new ArrayList<>();

    if (resultJsonObject.has(ELEMENT_LOCATIONS)) {

      final JsonArray locations = resultJsonObject.get(ELEMENT_LOCATIONS).getAsJsonArray();
      // Also render relatedLocations if required...

      locations.forEach(loc -> {
        final JsonObject locationJsonObject = loc.getAsJsonObject();
        if (locationJsonObject.has(ELEMENT_PHYSICAL_LOCATION)) {
          final JsonObject physicalLocation = locationJsonObject.get(ELEMENT_PHYSICAL_LOCATION).getAsJsonObject();
          final JsonObject artifactLocation = physicalLocation.get(ELEMENT_ARTIFACT_LOCATION).getAsJsonObject();
          result.add(Location.builder()
              .uri(getObjectAsStringIfExists(artifactLocation, ELEMENT_URI))
              .uriBaseId(getObjectAsStringIfExists(artifactLocation, ELEMENT_URI_BASE_ID))
              .index(getObjectAsIntegerIfExists(artifactLocation, ELEMENT_INDEX))
              .region(parseRegion(physicalLocation.get(ELEMENT_REGION).getAsJsonObject()))
              .build());
        }
      });
    }
    return result;
  }

  private static Region parseRegion(JsonObject region) {
    return Region.builder()
        .startLine(getObjectAsIntegerIfExists(region, ELEMENT_START_LINE))
        .startColumn(getObjectAsIntegerIfExists(region, ELEMENT_START_COLUMN))
        .endColumn(getObjectAsIntegerIfExists(region, ELEMENT_END_COLUMN))
        .build();
  }
}
