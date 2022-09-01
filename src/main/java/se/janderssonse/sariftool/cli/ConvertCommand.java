// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0
package se.janderssonse.sariftool.cli;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;

import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Option;
import se.janderssonse.sariftool.SarifParser;
import se.janderssonse.sariftool.mapper.Mapper;
import se.janderssonse.sariftool.mapper.MapperFactory;
import se.janderssonse.sariftool.mapper.Mapper.MapperType;
import se.janderssonse.sariftool.util.Util;

@Command(name = "convert", description = "Convert a SARIF file to another format", sortOptions = false)
public final class ConvertCommand implements Runnable {

    private static final Logger LOG = Logger.getLogger(SarifToolCLI.class.getName());

    @Option(names = { "-s", "--source(s)" }, required = true, description = "A /path/to/a/SARIF/dir/or/file.")
    private Path sourcePath;

    @Option(names = { "-o", "--output" }, required = true, description = "A /path/to/dir/for/output/.")
    private Path outputDir;

    @Option(names = { "-t",
            "--tool" }, showDefaultValue = Visibility.ALWAYS, defaultValue = "sonar", description = "Only sonar custom issue supper atm.")
    private String tool;

    @Option(names = { "-e",
            "--excludepath" }, showDefaultValue = Visibility.ALWAYS, defaultValue = "src/test", description = "A path to exclude")
    private List<String> excludePaths;

    @Override
    public void run() {

        fileToSarifParsers().stream()
                .filter(parser -> parser.validated())
                .forEach(parser -> getDefaultMappers(parser)
                        .forEach(mapper -> mapper.map(targetPath(parser.sarifFile()),
                                excludePaths)));
    }

    private List<SarifParser> fileToSarifParsers() {
        return sarifPaths(sourcePath).stream().map(sarifFile -> new SarifParser(sarifFile)).toList();
    }

    private List<Path> sarifPaths(final Path sourceFile) {

        if (Files.isRegularFile(sourceFile)) {
            return List.of(sourceFile);
        } else if (Files.isDirectory(sourceFile)) {
            return Util.findFiles(sourceFile, "sarif");
        } else {
            LOG.info(String.format("Input incorrect, was: %s. Please add a path to a valid SARIF dir or file.",
                    sourceFile.toAbsolutePath()));
            return List.of();
        }
    }

    private Path targetPath(final Path sarifFile) {
        return Paths.get(outputDir.toString(), Util.removeFileExtension(sarifFile, true).concat(".json"));
    }

    private List<Mapper> getDefaultMappers(final SarifParser parser) {
        MapperType format = MapperType.valueOf(tool.toUpperCase());
        Mapper sonarIssueMapper = MapperFactory.getMapper(format, parser);
        return List.of(sonarIssueMapper);
    }
}
