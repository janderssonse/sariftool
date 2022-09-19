
// SPDX-FileCopyrightText: 2021 Baloise Group
// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sarifconvert.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;

import de.dm.infrastructure.logcapture.LogCapture;
import picocli.CommandLine;

class SarifConvertCLITest {
    @TempDir
    File tmpDir;
    @RegisterExtension // use @Rule for LogCapture 2/JUnit 4
    public LogCapture logCapture = LogCapture.forCurrentPackage();

    @Test
    void testInvalidSarifFile() throws IllegalArgumentException {
        final File input = new File("src/test/resources/invalid.sarif");
        assertTrue(input.isFile());

        final SarifConvertCLI testee = new SarifConvertCLI();
        CommandLine cmd = new CommandLine(testee);
        int exitCode = cmd.execute("-s=" + input + " -o=" + tmpDir);

        assertEquals(0, exitCode);

    
        // assertTrue(exception.getMessage().startsWith("$schema not found in root
        // object"));
    }

    @Test
    void testHappyCase() throws Exception, IOException {
        testByResourceFiles2("src/test/resources/example.sarif", "src/test/resources/expectedResult.json", false);
    }

    @Test
    void testWithFilter() throws Exception, IOException {
        testByResourceFiles2("src/test/resources/InputWithTestResource.sarif", "src/test/resources/expectedResult.json",
                true);
    }

    @Test
    void multiModuleTest() throws Exception, IOException {
        testByResourceFiles2("src/test/resources/multiModuleInput.sarif", "src/test/resources/multiModuleResult.json",
                false);
    }

    private void testByResourceFiles2(String inputSarifFIle, String expectedResultFile, boolean ignoreTests)
            throws Exception, IOException {

        final File input = new File(inputSarifFIle).getAbsoluteFile();
        assertTrue(input.isFile());
        final File expected = new File(expectedResultFile);
        assertTrue(expected.isFile());

        final SarifConvertCLI app = new SarifConvertCLI();
        CommandLine cmd = new CommandLine(app);
        cmd.execute("-s=" + input, "-o=" + tmpDir);

        String nameWithoutExtenstion = SarifConvertCLI.removeFileExtension(input, true);
        String jsonPath = tmpDir.getAbsolutePath() + "/" + nameWithoutExtenstion + ".json";
        final String expectedString = Files.readString(expected.toPath()).trim();
        final String inputAsString = Files.readString(new File(jsonPath).toPath()).trim();
        assertEquals(expectedString, inputAsString);

    }

}
