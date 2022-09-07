// SPDX-FileCopyrightText: 2021 Baloise Group
// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0
package se.janderssonse.sarifconvert.cli;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import picocli.CommandLine.Help.ColorScheme;
import picocli.CommandLine.Help.Ansi.Style;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import se.janderssonse.sarifconvert.cli.sarif.ConsoleParser;
import se.janderssonse.sarifconvert.cli.sarif.SarifParser;
import se.janderssonse.sarifconvert.cli.sonar.SonarIssueMapper;

@Command(name = "sarifconvert", mixinStandardHelpOptions = true, version = "sarifconvert 0.1", description = "Convert SARIF format to other formats")
public class SarifConvertCLI implements Callable<Integer> {

    static Logger LOGGER = Logger.getLogger(SarifConvertCLI.class.getName());

    @Option(names = { "-i", "--inputdir" }, description = "A /path/to/dir/with/sarif/file(s)/.")
    private Path inputDir = Paths.get(".");

    @Option(names = { "-o", "--outputdir" }, description = "A /path/to/dir/for/output/.")
    private Path outputDir = Paths.get(".");

    @Option(names = { "-t", "--targetformat" }, description = "Only sonar atm (default).")
    private String targetFormat = "sonar";

    @Override
    public Integer call() {

        final List<File> sarifFiles = findSarifFiles(inputDir);

        sarifFiles.forEach(sarifFile -> {

            try {
                final SonarIssueMapper sonarIssueMapper = new SonarIssueMapper();

                LOGGER.info("Converting: ".concat(sarifFile.getAbsolutePath()));
                SarifParser.execute(sarifFile, new ConsoleParser(), sonarIssueMapper);
                LOGGER.info(sonarIssueMapper.getSummary());

                final StringWriter stringWriter = new StringWriter();
                final Gson gson = new GsonBuilder().setPrettyPrinting().create();

                new SonarIssueReporter().correctPathes(sonarIssueMapper);

                gson.toJson(sonarIssueMapper.getMappedIssues(null), stringWriter);
                LOGGER.info(stringWriter.toString());
                try (final FileWriter writer = new FileWriter(
                        outputDir + "/" + removeFileExtension(sarifFile, true) + ".json")) {
                    gson.toJson(sonarIssueMapper.getMappedIssues(new String[] { "/test/" }), writer);
                    writer.flush();
                }

            } catch (IOException e) {
                LOGGER.info("IOException" + e.getMessage());
                ;
            }
        });
        return 0;
    }

    public static void main(String... args) {
        int exitCode = new CommandLine(new SarifConvertCLI()).execute(args);
        System.exit(exitCode);
    }

    public static String removeFileExtension(File file, boolean removeAllExtensions) {
        if (file == null || !file.exists()) {
            return "";
        }

        String filename = file.getName();
        String extPattern = "(?<!^)[.]" + (removeAllExtensions ? ".*" : "[^.]*$");
        return filename.replaceAll(extPattern, "");
    }

    private List<File> findSarifFiles(Path dir) {

        try (Stream<Path> walk = Files.walk(dir)) {
            return walk
                    .filter(p -> !Files.isDirectory(p))
                    .map(p -> p.toString())
                    .filter(f -> f.endsWith("sarif"))
                    .map(f -> new File(f))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            LOGGER.info("IO failed" + e.getMessage());
            return Collections.EMPTY_LIST;
        }
    }

 public static CommandLine.Help.ColorScheme getColorScheme() {
        // see also CommandLine.Help.defaultColorScheme()
        return new ColorScheme.Builder()
                .commands(Style.bold, Style.underline) // combine multiple styles
                .options(Style.fg_yellow) // yellow foreground color
                .parameters(Style.fg_yellow)
                .optionParams(Style.italic)
                .errors(Style.fg_red, Style.bold)
                .stackTraces(Style.italic)
                .build();
    }
}