// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sariftool;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import se.janderssonse.sariftool.model.sarif.Driver;
import se.janderssonse.sariftool.model.sarif.Location;
import se.janderssonse.sariftool.model.sarif.Region;
import se.janderssonse.sariftool.model.sarif.Result;
import se.janderssonse.sariftool.model.sarif.Rule;
import se.janderssonse.sariftool.model.sarif.RuleProperties;
import se.janderssonse.sariftool.util.JsonWrapper;
import se.janderssonse.sariftool.util.Util;

public final class SarifParser {

    static final Logger LOG = Logger.getLogger(SarifParser.class.getName());

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
    static final String ELEMENT_END_LINE = "endLine";
    static final String ELEMENT_TAGS = "tags";
    static final String ELEMENT_TEXT = "text";
    static final String ELEMENT_TOOL = "tool";
    static final String ELEMENT_URI = "uri";
    static final String ELEMENT_URI_BASE_ID = "uriBaseId";
    static final String ELEMENT_VERSION = "version";

    private Optional<String> version = Optional.empty();
    private Optional<String> schema = Optional.empty();
    private boolean validated;
    private Path parseFile;

    private Optional<Driver> driverDto;
    private List<Rule> rules = List.of();
    private List<Result> results = List.of();

    public boolean validated() {
        return validated;
    }

    public Path sarifFile() {
        return parseFile;
    }

    public Optional<String> getVersion() {
        return version;
    }

    public Optional<String> getSchema() {
        return schema;
    }

    public Optional<Driver> getDriver() {
        return driverDto;
    }

    public List<Result> getResults() {
        return results;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public SarifParser(final Path sarifFile) {
        try {
            LOG.info("Parsing: ".concat(sarifFile.toAbsolutePath().toString()));
            parseFile = sarifFile;

            map(sarifFile);

            if (validated) {
                LOG.fine(String.format("Parsed %d rules, %d results from file %s.", rules.size(), results.size(),
                        sarifFile.toAbsolutePath().toString()));
            } else {
                LOG.fine(String.format("Parsed 0 rules, 0 results from invalid file %s.",
                        sarifFile.toAbsolutePath().toString()));
            }
        } catch (IllegalArgumentException | IOException e) {
            LOG.warning("Failed parsing: " + e.getMessage());
            LOG.severe("Failed parsing due to : " + e);
        }
    }

    private void parseRun(final JsonNode node) {
        parseRules(node);
        parseResults(node);
    }

    public void map(final Path sarifInputFile) throws IOException, FileNotFoundException, IllegalArgumentException {

        try (FileReader reader = new FileReader(sarifInputFile.toFile())) {

            JsonNode rootNode = JsonWrapper.toNode(reader);

            if (Util.schemaValidate(sarifInputFile)) {

                validated = true;

                if (rootNode.has(ELEMENT_VERSION)) {
                    version = asString(rootNode, SarifParser.ELEMENT_VERSION);
                }

                if (rootNode.has(ELEMENT_SCHEMA)) {
                    schema = asString(rootNode, SarifParser.ELEMENT_SCHEMA);
                }

                if (rootNode.has(ELEMENT_RUNS)) {
                    for (JsonNode singleRun : rootNode.get(ELEMENT_RUNS)) {
                        parseRun(singleRun);
                    }
                }
            }
        }

    }

    private void parseRules(final JsonNode node) {
        if (node.has(ELEMENT_TOOL)) {
            final JsonNode toolNode = node.get(ELEMENT_TOOL);

            if (toolNode.has(ELEMENT_DRIVER)) {
                final JsonNode driver = toolNode.get(ELEMENT_DRIVER);
                driverDto = toDriver(driver);

                if (!driver.isMissingNode() && driver.has(ELEMENT_RULES)) {
                    processRules(driver.get(ELEMENT_RULES));
                }
            }

            if (toolNode.has(ELEMENT_EXTENSIONS)) {
                final JsonNode extensions = toolNode.get(ELEMENT_EXTENSIONS);
                extensions.forEach(extension -> {
                    final JsonNode ext = extension;
                    if (ext.has(ELEMENT_RULES)) {
                        processRules(ext.get(ELEMENT_RULES));
                    }
                });
            }
        }
    }

    private void processRules(final JsonNode node) {
        Stream<JsonNode> s = StreamSupport.stream(node.spliterator(), false);
        rules = s.map(ruleNode -> new Rule(
                asString(ruleNode, ELEMENT_ID), asString(ruleNode, ELEMENT_NAME),
                toTextElement(ruleNode, ELEMENT_SHORT_DESCRIPTION),
                toTextElement(ruleNode, ELEMENT_FULL_DESCRIPTION), toRuleLevel(ruleNode),
                toRuleProperties(ruleNode))).toList();
    }

    private Optional<Driver> toDriver(final JsonNode node) {
        if (node.isMissingNode()) {
            return Optional.empty();
        }
        return Optional.of(new Driver(asString(node, ELEMENT_NAME),
                asString(node, ELEMENT_ORGANIZATION),
                asString(node, ELEMENT_SEMANTIC_VERSION)));
    }

    private Optional<Rule.Level> toRuleLevel(final JsonNode node) {
        if (node.has(ELEMENT_DEFAULT_CONFIGURATION)) {
            final JsonNode defaultConfig = node.get(ELEMENT_DEFAULT_CONFIGURATION);
            if (defaultConfig.has(ELEMENT_LEVEL)) {
                final Optional<String> levelAsString = asString(defaultConfig, ELEMENT_LEVEL);
                try {
                    return levelAsString.isEmpty() ? Optional.empty()
                            : Optional.of(Rule.Level.valueOf(levelAsString.get().toUpperCase(Locale.ROOT)));
                } catch (IllegalArgumentException e) {
                    LOG.warning(
                            String.format("Failed to interpret %s as Rule.Level: %s", levelAsString, e.getMessage()));
                }
            }
        }
        return Optional.empty();
    }

    private Optional<RuleProperties> toRuleProperties(final JsonNode node) {
        if (node.has(ELEMENT_PROPERTIES)) {
            final JsonNode properties = node.get(ELEMENT_PROPERTIES);
            return Optional.of(new RuleProperties(
                    asString(properties, ELEMENT_ID), asString(properties, ELEMENT_NAME),
                    asString(properties, ELEMENT_DESCRIPTION), Optional.of(toTags(properties)),
                    asString(properties, ELEMENT_KIND), asString(properties, ELEMENT_PRECISION),
                    toSeverity(properties)));
        }
        return Optional.empty();
    }

    private Optional<String> asString(final JsonNode node, final String elementId) {
        return !node.isMissingNode() && node.has(elementId) ? Optional.of(node.get(elementId).asText())
                : Optional.empty();
    }

    private Optional<Integer> asInt(final JsonNode node, final String elementId) {
        return !node.isMissingNode() && node.has(elementId) ? Optional.of(node.get(elementId).asInt())
                : Optional.empty();
    }

    private Optional<RuleProperties.Severity> toSeverity(final JsonNode node) {
        if (node.has(ELEMENT_PROBLEM_SEVERITY)) {
            final Optional<String> severityAsString = asString(node, ELEMENT_PROBLEM_SEVERITY);
            try {
                return Optional.of(RuleProperties.Severity.valueOf(severityAsString.orElse("")));
            } catch (IllegalArgumentException e) {
                LOG.warning(
                        String.format("Failed to interpret %s as RuleProperties.Severity: %s",
                                severityAsString.orElse(""),
                                e.getMessage()));
            }
        }
        return Optional.empty();
    }

    private ArrayList<String> toTags(final JsonNode node) {
        final HashSet<String> result = new HashSet<>();
        if (node.has(ELEMENT_TAGS)) {
            node.get(ELEMENT_TAGS).forEach(t -> result.add(t.asText()));
        }
        return new ArrayList<>(result);
    }

    private void parseResults(final JsonNode node) {
        if (node.has(ELEMENT_RESULTS)) {
            Stream<JsonNode> a = StreamSupport.stream(node.get(ELEMENT_RESULTS).spliterator(), false);
            results = a.map(result -> {
                final JsonNode resultJsonObject = result;

                Optional<Integer> resultIndex = asInt(resultJsonObject, ELEMENT_RULE_INDEX);
                if (resultIndex.isEmpty() && resultJsonObject.has(ELEMENT_RULE)) {
                    resultIndex = asInt(resultJsonObject
                            .get(ELEMENT_RULE), ELEMENT_INDEX);
                }
                Result resultDto = new Result(asString(resultJsonObject, ELEMENT_RULE_ID), resultIndex,
                        toTextElement(resultJsonObject, ELEMENT_MESSAGE), Optional.of(toLocations(resultJsonObject)));
                return resultDto;
            }).toList();

        } else {
            results = List.of();
        }
    }

    private Optional<String> toTextElement(final JsonNode node, final String parentProperty) {
        JsonNode element = node.get(parentProperty);
        JsonNode ob = element != null ? element : JsonNodeFactory.instance.objectNode();
        return asString(ob, ELEMENT_TEXT);
    }

    private List<Location> toLocations(final JsonNode node) {
        final ArrayList<Location> result = new ArrayList<>();

        if (node.has(ELEMENT_LOCATIONS)) {

            final JsonNode locations = node.get(ELEMENT_LOCATIONS);
            // Also render relatedLocations if required...

            locations.forEach(loc -> {
                final JsonNode locationJsonObject = loc;
                if (locationJsonObject.has(ELEMENT_PHYSICAL_LOCATION)) {
                    final JsonNode physicalLocation = locationJsonObject.get(ELEMENT_PHYSICAL_LOCATION);
                    final JsonNode artifactLocation = physicalLocation.get(ELEMENT_ARTIFACT_LOCATION);
                    result.add(new Location(
                            asString(artifactLocation, ELEMENT_URI).orElse(""),
                            asString(artifactLocation, ELEMENT_URI_BASE_ID),
                            asInt(artifactLocation, ELEMENT_INDEX),
                            Optional.of(toRegion(physicalLocation.get(ELEMENT_REGION)))));
                }
            });
        }
        return result;
    }

    private Region toRegion(final JsonNode node) {
        return new Region(
                asInt(node, ELEMENT_START_LINE).get(),
                asInt(node, ELEMENT_END_LINE),
                asInt(node, ELEMENT_START_COLUMN),
                asInt(node, ELEMENT_END_COLUMN));
    }

}
