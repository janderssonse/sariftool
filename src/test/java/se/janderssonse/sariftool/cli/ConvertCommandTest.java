// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0
package se.janderssonse.sariftool.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.JsonNode;

import picocli.CommandLine;
import se.janderssonse.sariftool.util.JsonWrapper;
import se.janderssonse.sariftool.util.Util;

class ConvertCommandTest {
    @TempDir
    File tmpDir;


    @Test
    void testInvalidSarifFile() throws IllegalArgumentException {
        final File input = new File("src/test/resources/invalid.sarif");
        assertTrue(input.isFile());

        final SarifToolCLI testee = new SarifToolCLI();
        CommandLine cmd = new CommandLine(testee);

        int exitCode = cmd.execute("convert", "-s=" + input, "-o=" + tmpDir);

        assertEquals(0, exitCode);

        // assertTrue(exception.getMessage().startsWith("$schema not found in root
        // object"));
    }

    @Test
    void testHappyCase() throws Exception, IOException {
        outputFileEqualsInputFile("src/test/resources/exampleWithTestDir.sarif", "src/test/resources/expectedResult.json");
    }

    @Test
    void multiModuleTest() throws Exception, IOException {
        outputFileEqualsInputFile("src/test/resources/multiModuleInput.sarif",
                "src/test/resources/multiModuleResult.json");
    }

    private void outputFileEqualsInputFile(String inputSarifFIle, String expectedResultFile)
            throws Exception, IOException {

        final File input = new File(inputSarifFIle).getAbsoluteFile();
        assertTrue(input.isFile());
        final File expected = new File(expectedResultFile);
        assertTrue(expected.isFile());

        final SarifToolCLI app = new SarifToolCLI();
        CommandLine cmd = new CommandLine(app);
        cmd.execute("convert", "-s=" + input, "-o=" + tmpDir);

        String nameWithoutExtenstion = Util.removeFileExtension(input.toPath(), true);
        String jsonPath = tmpDir.getAbsolutePath() + "/" + nameWithoutExtenstion + ".json";
        JsonNode a = JsonWrapper.toNode(expected);
        JsonNode b = JsonWrapper.toNode(new File(jsonPath));

        assertEquals(JsonWrapper.toJson(b), JsonWrapper.toJson(a));

    }

}
