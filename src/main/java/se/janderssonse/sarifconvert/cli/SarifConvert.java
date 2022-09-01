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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import se.janderssonse.sarifconvert.cli.sarif.ParserCallback;
import se.janderssonse.sarifconvert.cli.sarif.SarifParser;
import se.janderssonse.sarifconvert.cli.sarif.dto.Driver;
import se.janderssonse.sarifconvert.cli.sarif.dto.Result;
import se.janderssonse.sarifconvert.cli.sarif.dto.Rule;
import se.janderssonse.sarifconvert.cli.sonar.SonarIssueMapper;

@Command(name = "sarifconvert", mixinStandardHelpOptions = true, version = "sarifconvert 0.1", description = "Convert SARIF format to other formats")
public class SarifConvert implements Callable<Integer> {

    static Logger LOGGER = Logger.getLogger(SarifConvert.class.getName());
    @Option(names = { "-i", "--inputdir" }, description = "/path/to/dir/with/sarif/file(s)/")
    private Path inputDir = Paths.get(".");

    @Option(names = { "-o", "--outputdir" }, description = "/path/to/dir/for/output/")
    private Path outputDir = Paths.get(".");

    @Option(names = { "-t", "--targetformat" }, description = "sonar")
    private String targetFormat = "sonar";

    @Override
    public Integer call() {

        final List<File> sarifFiles = findSarifFiles(inputDir);

        sarifFiles.forEach(sarifFile -> {

            try {
                final SonarIssueMapper sonarIssueMapper = new SonarIssueMapper();

                LOGGER.info("Converting: ".concat(sarifFile.getAbsolutePath()));
                SarifParser.execute(sarifFile, logParser, sonarIssueMapper);
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
        int exitCode = new CommandLine(new SarifConvert()).execute(args);
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

    private static ParserCallback logParser = new ParserCallback() {
        @Override
        public void onFinding(Result result) {
            LOGGER.info(result.toString());
        }

        @Override
        public void onVersion(String version) {
            LOGGER.info(version);
        }

        @Override
        public void onSchema(String schema) {
            LOGGER.info(schema);
        }

        @Override
        public void onDriver(Driver driver) {
            LOGGER.info(driver.toString());
        }

        @Override
        public void onRule(Rule rule) {
            LOGGER.info(rule.toString());
        }
    };

}