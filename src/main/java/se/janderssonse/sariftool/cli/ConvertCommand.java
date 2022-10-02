// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0
package se.janderssonse.sariftool.cli;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Help.Visibility;
import se.janderssonse.sariftool.cli.sonar.dto.ImmutableSonarLocation;
import se.janderssonse.sariftool.cli.sarif.ConsoleParser;
import se.janderssonse.sariftool.cli.sarif.SarifParser;
import se.janderssonse.sariftool.cli.sonar.SonarIssueMapper;
import se.janderssonse.sariftool.cli.sonar.dto.Issues;

@Command(name = "convert", description = "Convert SARIF file to another format", sortOptions = false)
public final class ConvertCommand implements Runnable {

    private static Logger logger = Logger.getLogger(SarifToolCLI.class.getName());
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Option(names = { "-s", "--source(s)" }, required = true, description = "A /path/to/a/SARIF/dir/or/file.")
    private Path sourcePath;

    @Option(names = { "-o", "--output" }, required = true, description = "A /path/to/dir/for/output/.")
    private Path outputDir;

    /*
     * @Option(names = { "-t",
     * "--tool" }, showDefaultValue = Visibility.ALWAYS, defaultValue = "sonar",
     * description = "Only sonar custom issue supper atm.")
     * private String tool;
     */

    @Option(names = { "-e",
            "--excludepath" }, showDefaultValue = Visibility.ALWAYS, defaultValue = "src/test", description = "A path to exclude")
    private List<String> excludePaths;

    @Override
    public void run() {

        collectSarifPaths(sourcePath).forEach(sarifFile -> {

            logger.info("Converting: ".concat(sarifFile.getAbsolutePath()));

            try {
                final SonarIssueMapper sonarIssueMapper = new SonarIssueMapper();

                SarifParser.execute(sarifFile, new ConsoleParser(), sonarIssueMapper);
                logger.fine(sonarIssueMapper.getSummary());

                correctPathes(sonarIssueMapper);
                writeResult(sonarIssueMapper, sarifFile);

            } catch (IOException | IllegalArgumentException e) {
                logger.severe("Error:" + e.getMessage());
            }
        });
    }

    private List<File> collectSarifPaths(final Path sourceFile) {
        if (Files.isRegularFile(sourceFile)) {
            return new ArrayList<>(List.of(sourceFile.toFile()));
        } else if (Files.isDirectory(sourceFile)) {
            return findSarifFiles(sourceFile);
        } else {
            logger.info(String.format("Input incorrect, was: %s. Please add a path to a valid SARIF dir or file.",
                    sourceFile.toAbsolutePath()));
        }
        return Collections.emptyList();
    }

    private void writeResult(final SonarIssueMapper sonarIssueMapper, final File sarifFile) throws IOException {
        logger.fine("Excluded: " + excludePaths);
        Issues mappedIssues = sonarIssueMapper.getMappedIssues(excludePaths.toArray(new String[0]));
        final String targetPath = outputDir + "/" + removeFileExtension(sarifFile, true) + ".json";
        try (FileWriter writer = new FileWriter(targetPath)) {
            gson.toJson(mappedIssues, writer);
            writer.flush();
        }

        logger.fine(String.format("Writing to target '%s' containing %d issues.", targetPath,
                mappedIssues.getResult().size()));
    }

    public static String removeFileExtension(final File file, final boolean removeAllExtensions) {
        if (file == null || !file.exists()) {
            return "";
        }

        String filename = file.getName();
        String extPattern = "(?<!^)[.]" + (removeAllExtensions ? ".*" : "[^.]*$");
        return filename.replaceAll(extPattern, "");
    }

    private List<File> findSarifFiles(final Path dir) {

        try (Stream<Path> walk = Files.walk(dir)) {
            return walk
                    .filter(p -> !Files.isDirectory(p))
                    .map(p -> p.toString())
                    .filter(f -> f.endsWith("sarif"))
                    .map(f -> new File(f))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            logger.severe("IO failed" + e.getMessage());
            return Collections.EMPTY_LIST;
        }
    }

    /**
     * Remove module prefix in filePath in case of multiModuleBuild.
     */
    public static void correctPathes(final SonarIssueMapper sonarIssueMapper) {
        final List<String> srcDirPom = getSourceDirectoryFromPom(null);
        // getPluginContext());

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
                            logger.fine(String.format("Replace '%s' with '%s'", filePath, replacedPath));
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
