// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0
package se.janderssonse.sariftool.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.vertx.json.schema.Draft;
import io.vertx.json.schema.JsonSchema;
import io.vertx.json.schema.JsonSchemaOptions;
import io.vertx.json.schema.OutputUnit;
import io.vertx.json.schema.Validator;

import java.util.logging.Logger;

public final class Util {

    private Util() {
    }

    private static final Logger LOG = Logger.getLogger(Util.class.getName());

    public static String removeFileExtension(final Path file, final boolean removeAllExtensions) {
        return file.getFileName().toString().replaceAll("\\.\\w+$", "");
    }

    public static List<Path> findFiles(final Path dir, final String suffix) {

        try (Stream<Path> walk = Files.walk(dir)) {
            return walk
                    .filter(file -> !Files.isDirectory(file))
                    .map(it -> it.toString())
                    .filter(path -> path.endsWith(suffix))
                    .map(path -> Paths.get(path))
                    .toList();
        } catch (IOException e) {
            LOG.severe("IO failed" + e.toString());
            return List.of();
        }
    }

    public static boolean schemaValidate(final Path sarifFile) throws IllegalArgumentException {
        try {
            InputStream s = Util.class.getResourceAsStream("/sarif-schema-2.1.0.json");

            String schematext = new String(s.readAllBytes(), StandardCharsets.UTF_8);
            String filetext = Files.readString(sarifFile);
            io.vertx.core.json.JsonObject json = new io.vertx.core.json.JsonObject(filetext);
            JsonSchema schema = JsonSchema.of(new io.vertx.core.json.JsonObject(schematext));
            OutputUnit result = Validator.create(
                    schema,
                    new JsonSchemaOptions().setDraft(Draft.DRAFT202012).setBaseUri("http://"))
                    .validate(json);
            LOG.fine("validation " + result.toString());
            if (!result.getValid()) {
                throw new IllegalArgumentException(
                        String.format("Validation failed: %s Err: %s ", sarifFile.getFileName(),
                                result.getErrors().stream().map(o -> o.toString()).collect(Collectors.joining("\n"))));
            }

        } catch (IOException e) {
            LOG.severe("validation failed" + e.getMessage());
        }
        return true;
    }
}
