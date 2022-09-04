// SPDX-FileCopyrightText: 2021 Baloise Group
// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sarifconvert.cli.sarif;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import se.janderssonse.sarifconvert.cli.sarif.dto.ImmutableDriver;
import se.janderssonse.sarifconvert.cli.sarif.dto.ImmutableLocation;
import se.janderssonse.sarifconvert.cli.sarif.dto.ImmutableRegion;
import se.janderssonse.sarifconvert.cli.sarif.dto.ImmutableResult;
import se.janderssonse.sarifconvert.cli.sarif.dto.ImmutableRule;
import se.janderssonse.sarifconvert.cli.sarif.dto.ImmutableRuleProperties;
import se.janderssonse.sarifconvert.cli.sarif.dto.Location;
import se.janderssonse.sarifconvert.cli.sarif.dto.Region;
import se.janderssonse.sarifconvert.cli.sarif.dto.Result;
import se.janderssonse.sarifconvert.cli.sarif.dto.Rule;
import se.janderssonse.sarifconvert.cli.sarif.dto.RuleProperties;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.StackWalker.Option;
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
   * For the result handling multiple
   * callback handler implementing
   * the @{@link ParserCallback} can be provided.
   *
   * @throws FileNotFoundException when sarifInputFile is not present
   */
  public static void execute(File sarifInputFile, ParserCallback... callback)
      throws IOException, FileNotFoundException {

    try (final FileReader reader = new FileReader(sarifInputFile)) {

      final JsonObject rootObject = JsonParser.parseReader(reader).getAsJsonObject();

      if (validate(rootObject, sarifInputFile)) {

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
        final ImmutableDriver driverDto = parseDriver(driver);
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
      final ImmutableRule ruleDto = ImmutableRule.builder()
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

  private static ImmutableDriver parseDriver(JsonObject driver) {
    if (driver == null) {
      return null;
    }
    return ImmutableDriver.builder()
        .name(getObjectAsStringIfExists(driver, ELEMENT_NAME))
        .organization(getObjectIfExists(driver, ELEMENT_ORGANIZATION))
        .semanticVersion(getObjectAsStringIfExists(driver, ELEMENT_SEMANTIC_VERSION))
        .build();
  }

  private static Optional<ImmutableRule.Level> parseDefaultConfigLevel(JsonObject jsonObjectRule) {
    if (jsonObjectRule.has(ELEMENT_DEFAULT_CONFIGURATION)) {
      final JsonObject defaultConfig = jsonObjectRule.get(ELEMENT_DEFAULT_CONFIGURATION).getAsJsonObject();
      if (defaultConfig.has(ELEMENT_LEVEL)) {
        final Optional<String> levelAsString = getObjectIfExists(defaultConfig, ELEMENT_LEVEL);
        try {
          return levelAsString.isEmpty() ? Optional.empty() : Optional.of(ImmutableRule.Level.valueOf(levelAsString.get().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
          LOGGER.warning(String.format("Failed to interpret %s as Rule.Level: %s", levelAsString, e.getMessage()));
        }
      }
    }
    return Optional.empty();
  }

  private static ImmutableRuleProperties parseRuleProperties(JsonObject jsonObjectRule) {
    if (jsonObjectRule.has(ELEMENT_PROPERTIES)) {
      final JsonObject properties = jsonObjectRule.get(ELEMENT_PROPERTIES).getAsJsonObject();
      return ImmutableRuleProperties.builder()
          .id(getObjectIfExists(properties, ELEMENT_ID))
          .name(getObjectIfExists(properties, ELEMENT_NAME))
          .description(getObjectIfExists(properties, ELEMENT_DESCRIPTION))
          .kind(getObjectIfExists(properties, ELEMENT_KIND))
          .precision(getObjectIfExists(properties, ELEMENT_PRECISION))
          .tags(parseTags(properties))
          .severity(parseProblemSeverity(properties))
          .build();
    }
    return null;
  }

  private static String getObjectAsStringIfExists(JsonObject jsonObject, String elementId) {
    return jsonObject != null && jsonObject.has(elementId) ? jsonObject.get(elementId).getAsString() : null;
  }

  private static Optional<String> getObjectIfExists(JsonObject jsonObject, String elementId) {
    return jsonObject != null && jsonObject.has(elementId) ? Optional.of(jsonObject.get(elementId).getAsString())
        : Optional.empty();
  }

  private static Optional<Integer> getObjectIntIfExists(JsonObject jsonObject, String elementId) {
    return jsonObject != null && jsonObject.has(elementId) ? Optional.of(jsonObject.get(elementId).getAsInt())
        : Optional.empty();
  }

  private static Integer getObjectAsIntegerIfExists(JsonObject jsonObject, String elementId) {
    return jsonObject != null && jsonObject.has(elementId) ? jsonObject.get(elementId).getAsInt() : null;
  }

  private static Optional<ImmutableRuleProperties.Severity> parseProblemSeverity(JsonObject properties) {
    if (properties.has(ELEMENT_PROBLEM_SEVERITY)) {
      final Optional<String> severityAsString = getObjectIfExists(properties, ELEMENT_PROBLEM_SEVERITY);
      try {
        return Optional.of(ImmutableRuleProperties.Severity.valueOf(severityAsString.orElse("")));
      } catch (IllegalArgumentException e) {
        LOGGER.warning(
            String.format("Failed to interpret %s as RuleProperties.Severity: %s", severityAsString.orElse(""), e.getMessage()));
      }
    }
    return Optional.empty();
  }

  private static ArrayList<String> parseTags(JsonObject properties) {
    final HashSet<String> result = new HashSet<>();
    if (properties.has(ELEMENT_TAGS)) {
      properties.get(ELEMENT_TAGS).getAsJsonArray().forEach(t -> result.add(t.getAsString()));
    }
    return new ArrayList<>(result);
  }

  private static void parseResults(JsonObject run, ParserCallback[] callback) {
    if (run.has(ELEMENT_RESULTS)) {
      run.get(ELEMENT_RESULTS).getAsJsonArray().forEach(result -> {
        final JsonObject resultJsonObject = result.getAsJsonObject();
        final ImmutableResult.Builder resultDto = ImmutableResult.builder()
            .ruleId(getObjectAsStringIfExists(resultJsonObject, ELEMENT_RULE_ID))
            .message(getTextElement(resultJsonObject, ELEMENT_MESSAGE))
            .locations(parseLocations(resultJsonObject));

        Optional<Integer> resultIndex = getObjectIntIfExists(resultJsonObject, ELEMENT_RULE_INDEX);
        if (resultIndex.isEmpty() && resultJsonObject.has(ELEMENT_RULE)) {
          resultDto.ruleIndex(getObjectIntIfExists(resultJsonObject
              .getAsJsonObject(ELEMENT_RULE), ELEMENT_INDEX));
        } else {
          resultDto.ruleIndex(resultIndex);
        }
        ImmutableResult a = resultDto.build();

        Arrays.stream(callback).forEach(cb -> cb.onFinding(resultDto.build()));
      });
    }
  }

  private static String getTextElement(JsonObject object, String parentProperty) {
    return getObjectAsStringIfExists(object.get(parentProperty).getAsJsonObject(), ELEMENT_TEXT);
  }

  private static List<ImmutableLocation> parseLocations(JsonObject resultJsonObject) {
    final ArrayList<ImmutableLocation> result = new ArrayList<>();

    if (resultJsonObject.has(ELEMENT_LOCATIONS)) {

      final JsonArray locations = resultJsonObject.get(ELEMENT_LOCATIONS).getAsJsonArray();
      // Also render relatedLocations if required...

      locations.forEach(loc -> {
        final JsonObject locationJsonObject = loc.getAsJsonObject();
        if (locationJsonObject.has(ELEMENT_PHYSICAL_LOCATION)) {
          final JsonObject physicalLocation = locationJsonObject.get(ELEMENT_PHYSICAL_LOCATION).getAsJsonObject();
          final JsonObject artifactLocation = physicalLocation.get(ELEMENT_ARTIFACT_LOCATION).getAsJsonObject();
          result.add(ImmutableLocation.builder()
              .uri(getObjectAsStringIfExists(artifactLocation, ELEMENT_URI))
              .uriBaseId(getObjectIfExists(artifactLocation, ELEMENT_URI_BASE_ID))
              .index(getObjectIntIfExists(artifactLocation, ELEMENT_INDEX))
              .region(parseRegion(physicalLocation.get(ELEMENT_REGION).getAsJsonObject()))
              .build());
        }
      });
    }
    return result;
  }

  private static ImmutableRegion parseRegion(JsonObject region) {
    return ImmutableRegion.builder()
        .startLine(getObjectAsIntegerIfExists(region, ELEMENT_START_LINE))
        .startColumn(getObjectIntIfExists(region, ELEMENT_START_COLUMN))
        .endColumn(getObjectIntIfExists(region, ELEMENT_END_COLUMN))
        .build();
  }

  /*
   * Todo: Currently this is wrong - a minimal ok SARIF file is actuallly
   * resources/minimalValidSarifLogFileK1.sarif, taken
   * from the official examples.
   */
  private static Boolean validate(JsonObject rootObject, File sarifFile) {
    if (!rootObject.has("$schema")) {
      throw new IllegalArgumentException(
          String.format("$schema not found in root object - provided file %s does not seem to be a valid sarif file",
              sarifFile.getName()));
    }
    return true;
  }
}
