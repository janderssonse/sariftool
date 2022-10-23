// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0
package se.janderssonse.sariftool.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

public class JsonWrapper {

    private static ObjectMapper mapper;
    private static final Logger LOG = Logger.getLogger(JsonWrapper.class.getName());

    private static ObjectMapper getMapper() {
        if (mapper == null) {
            mapper = JsonMapper.builder().addModule(new Jdk8Module())
                    .serializationInclusion(Include.NON_ABSENT)
                    .configure(MapperFeature.SORT_CREATOR_PROPERTIES_FIRST, true).build();
        }
        return mapper;
    }

    public static void toFile(final Path path, final Object obj) {
        LOG.fine("Writing: " + obj.toString() + " with path: " + path);
        try {
            getMapper().writerWithDefaultPrettyPrinter().writeValue(path.toFile(), obj);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Could not write file" + path + " with object " + obj + " " + e.toString());
        }

    }

    public static JsonNode toNode(final FileReader reader) throws IOException {
        return getMapper().readTree(reader);
    }

    public static JsonNode toNode(final File file) throws IOException {
        return getMapper().readTree(new FileReader(file));
    }

    public static String toJson(final Object object) throws IOException {
        return getMapper().writeValueAsString(object);
    }
}
