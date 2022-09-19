// SPDX-FileCopyrightText: 2021 Baloise Group
// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0
package se.janderssonse.sarifconvert.cli;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.ConsoleHandler;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi.Style;
import picocli.CommandLine.Help.ColorScheme;
import picocli.CommandLine.Option;
import se.janderssonse.sarifconvert.cli.sarif.ConsoleParser;
import se.janderssonse.sarifconvert.cli.sarif.SarifParser;
import se.janderssonse.sarifconvert.cli.sonar.SonarIssueMapper;
import se.janderssonse.sarifconvert.cli.sonar.dto.ImmutableSonarLocation;
import se.janderssonse.sarifconvert.cli.sonar.dto.Issues;

@Command(name = "sarifconvert", mixinStandardHelpOptions = true, version = "sarifconvert 0.1", description = "Convert SARIF format to other formats")
public class SarifConvertCLI implements Callable<Integer> {

    static Logger LOGGER = Logger.getLogger(SarifConvertCLI.class.getName());

    @Option(names = { "-s", "--source(s)" }, description = "A /path/to/a/SARIF/dir/or/file.")
    private Path sourcePath = Paths.get(".");

    @Option(names = { "-o", "--targetdir" }, description = "A /path/to/dir/for/output/.")
    private Path outputDir = Paths.get(".");

    @Option(names = { "-f", "--targetformat" }, description = "Only sonar atm (default).")
    private String targetFormat = "sonar";

    @Option(names = { "-e", "--excludepath" }, description = "Exclude path")
    private List<String> excludePaths = List.of("src/test");

    final static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public Integer call() {

        collectSarifPaths(sourcePath).forEach(sarifFile -> {

            LOGGER.info("Converting: ".concat(sarifFile.getAbsolutePath()));

            try {
                final SonarIssueMapper sonarIssueMapper = new SonarIssueMapper();

                SarifParser.execute(sarifFile, new ConsoleParser(), sonarIssueMapper);
                LOGGER.fine(sonarIssueMapper.getSummary());

                correctPathes(sonarIssueMapper);
                writeResult(sonarIssueMapper, sarifFile);

            } catch (IOException | IllegalArgumentException e) {
                LOGGER.severe("Error:" + e.getMessage());
            }
        });
        return 0;
    }

    private List<File> collectSarifPaths(Path sourceFile) {
        if (Files.isRegularFile(sourceFile)) {
            return new ArrayList<>(List.of(sourceFile.toFile()));
        } else if (Files.isDirectory(sourceFile)) {
            return findSarifFiles(sourceFile);
        } else {
            LOGGER.info(String.format("Input incorrect, was: %s. Please add a path to a valid SARIF dir or file.",
                    sourceFile.toAbsolutePath()));
        }
        return Collections.emptyList();
    }



    public static void main(String... args) {
        int exitCode = new CommandLine(new SarifConvertCLI()).execute(args);
        System.exit(exitCode);
    }

    private void writeResult(SonarIssueMapper sonarIssueMapper, File sarifFile) throws IOException {
        LOGGER.fine("Excluded: " + excludePaths);
        final Issues mappedIssues = sonarIssueMapper.getMappedIssues(excludePaths.toArray(new String[0]));
        final String targetPath = outputDir + "/" + removeFileExtension(sarifFile, true) + ".json";
        try (final FileWriter writer = new FileWriter(targetPath)) {
            gson.toJson(mappedIssues, writer);
            writer.flush();
        }

        LOGGER.fine(String.format("Writing to target '%s' containing %d issues.", targetPath,
                mappedIssues.getResult().size()));
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
            LOGGER.severe("IO failed" + e.getMessage());
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

    /**
     * remove module prefix in filePath in case of multiModuleBuild
     */
    public static void correctPathes(SonarIssueMapper sonarIssueMapper) {
        final List<String> srcDirPom = getSourceDirectoryFromPom(null);// getPluginContext());

        sonarIssueMapper.getMappedIssues(null).getResult().forEach(issue -> {
            if (issue.primaryLocation().isPresent()) {
                // process each mapped issue
                final String filePath = issue.primaryLocation().get().filePath();
                srcDirPom.stream()
                        .map(s -> s.split("/")[0] + "/") // consider only first folder (e.g., src/) in order to capture
                                                         // generated
                                                         // folders also
                        // if filepath contains dir but does not start with it, it seems to be prefixed
                        // by module name
                        .filter(srcDirFilter -> !filePath.startsWith(srcDirFilter) && filePath.contains(srcDirFilter))
                        .findFirst()
                        .ifPresent(path2Fix -> {
                            // remove module name
                            final String replacedPath = filePath.substring(filePath.indexOf("/" + path2Fix) + 1);
                            LOGGER.fine(String.format("Replace '%s' with '%s'", filePath, replacedPath));
                            issue.withPrimaryLocation(
                                    ImmutableSonarLocation.builder().from(issue.primaryLocation().get())
                                            .filePath(replacedPath).build());
                        });
            }
        });
    }

    static List<String> getSourceDirectoryFromPom(final Map pluginContext) {
        final List<String> defaults = Collections.singletonList("src/");

        if (pluginContext == null) {
            return defaults;
        }

        // final MavenProject project = (MavenProject) pluginContext.get("project");
        final List<String> sourceRoots = new ArrayList<>();
        // sourceRoots.addAll(project.getCompileSourceRoots());
        // sourceRoots.addAll(project.getTestCompileSourceRoots());

        if (sourceRoots.isEmpty()) {
            return defaults;
        }

        final String absolutePath = null; // project.getBasedir().getAbsolutePath() + File.separator;
        return sourceRoots.stream()
                .map(s -> s.replace(absolutePath, "").trim())
                .map(s -> s.replace("\\", "/"))
                .collect(Collectors.toList());
    }
}