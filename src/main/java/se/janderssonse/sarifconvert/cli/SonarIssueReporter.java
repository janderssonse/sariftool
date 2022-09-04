// SPDX-FileCopyrightText: 2021 Baloise Group
// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0

package se.janderssonse.sarifconvert.cli;


import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import se.janderssonse.sarifconvert.cli.sarif.ConsoleParser;
import se.janderssonse.sarifconvert.cli.sarif.SarifParser;
import se.janderssonse.sarifconvert.cli.sonar.SonarIssueMapper;
import se.janderssonse.sarifconvert.cli.sonar.dto.ImmutableSonarLocation;
import se.janderssonse.sarifconvert.cli.sonar.dto.Issues;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SonarIssueReporter {


  static Logger LOGGER = Logger.getLogger(SonarIssueReporter.class.getName());
  
  private static final String ERR_FILE_SUFFIX = "Verify parameter codeql2sonar.sarif.inputfile in your pom.xml";
  private static final String DEFAULT_OURPUT_FILE = "./target/sonar/codeql2sonar.json";
  private static final String DEFAULT_IGNORE_TEST_FLAG = "false";

  //@Parameter(property = "codeql2sonar.sarif.inputfile")
  private String sarifInputFile;

  //@Parameter(property = "codeql2sonar.sarif.outputfile", defaultValue = DEFAULT_OURPUT_FILE)
  private String target;

  //@Parameter(property = "codeql2sonar.sarif.ignoreTests", defaultValue = DEFAULT_IGNORE_TEST_FLAG)
  private boolean ignoreTests;

  //@Parameter(property = "codeql2sonar.sarif.path.excludes")
  private String[] pathExlcudes;

  private Writer writer;

  public void setWriter(Writer writer) {
    this.writer = writer;
  }

  public SonarIssueReporter(String sarifInputFile) {
    this.sarifInputFile = sarifInputFile;
    /* set defaults */
    this.target = DEFAULT_OURPUT_FILE.replace("/", File.separator);
    this.ignoreTests = false;
  }

  public SonarIssueReporter(String sarifInputFile, String target, boolean ignoreTests, String[] pathExlcudes) {
    this.sarifInputFile = sarifInputFile;
    this.target = target;
    this.ignoreTests = ignoreTests;
    this.pathExlcudes = pathExlcudes;
  }

  public SonarIssueReporter() {
  }

  public void execute() throws Exception {
    LOGGER.info("execute SonarIssueReporter");
    try {
      final SonarIssueMapper sonarIssueMapper = new SonarIssueMapper();
      final File inputFile = readSarifFile(this.sarifInputFile);
      SarifParser.execute(inputFile, new ConsoleParser(), sonarIssueMapper);
      correctPathes(sonarIssueMapper);
      try (final Writer resultWriter = getWriter()) {
        writeResult(sonarIssueMapper, resultWriter);
      }
    } catch (Exception e) {
      final StackTraceElement[] stackTrace = e.getStackTrace();
      final String errMsg = (stackTrace != null && stackTrace.length>0)
                                ? e.getMessage() + System.lineSeparator() + stackTrace[0].toString()
                                : e.getMessage();
      LOGGER.log(Level.FINE,e.getMessage());
      throw new RuntimeException(errMsg);
    }
  }

  /**
   * remove module prefix in filePath in case of multiModuleBuild
   */
  void correctPathes(SonarIssueMapper sonarIssueMapper) {
    final List<String> srcDirPom = getSourceDirectoryFromPom(null);//getPluginContext());

    sonarIssueMapper.getMappedIssues(null).getResult().forEach(issue -> {
      //process each mapped issue
      final String filePath = issue.primaryLocation().get().filePath();
      srcDirPom.stream()
          .map(s -> s.split("/")[0] + "/") // consider only first folder (e.g., src/) in order to capture generated folders also
          // if filepath contains dir but does not start with it, it seems to be prefixed by module name
          .filter(srcDirFilter -> !filePath.startsWith(srcDirFilter) && filePath.contains(srcDirFilter)).findFirst()
          .ifPresent(path2Fix -> {
            // remove module name
            final String replacedPath = filePath.substring(filePath.indexOf("/" + path2Fix) + 1);
            LOGGER.log(Level.FINE,String.format("Replace '%s' with '%s'", filePath, replacedPath));
            issue.withPrimaryLocation(ImmutableSonarLocation.builder().from(issue.primaryLocation().get()).filePath(replacedPath).build());
          });
    });
  }

  List<String> getSourceDirectoryFromPom(final Map pluginContext) {
    final List<String> defaults = Collections.singletonList("src/");

    if (pluginContext == null) {
      return defaults;
    }

//    final MavenProject project = (MavenProject) pluginContext.get("project");
    final List<String> sourceRoots = new ArrayList<>();
 //   sourceRoots.addAll(project.getCompileSourceRoots());
  //  sourceRoots.addAll(project.getTestCompileSourceRoots());

    if (sourceRoots.isEmpty()) {
      return defaults;
    }

    final String absolutePath =  null; //project.getBasedir().getAbsolutePath() + File.separator;
    return sourceRoots.stream()
               .map(s -> s.replace(absolutePath, "").trim())
               .map(s -> s.replace("\\", "/"))
               .collect(Collectors.toList());
  }

  private Writer getWriter() throws IOException {
    return (writer == null) ? new FileWriter(target) : writer;
  }

  private void writeResult(SonarIssueMapper sonarIssueMapper, Writer writer) throws IOException {
    final String[] patternsToExclude = getPatternsToExclude();
    LOGGER.log(Level.FINE,"patterns to exclude: " + Arrays.toString(patternsToExclude));

    final Issues mappedIssues = sonarIssueMapper.getMappedIssues(patternsToExclude);
    LOGGER.info(String.format("Writing target '%s' containing %d issues.", target, mappedIssues.getResult().size()));

    new GsonBuilder().setPrettyPrinting().create().toJson(mappedIssues, writer);
    writer.flush();
  }

  private String[] getPatternsToExclude() {
    if (ignoreTests) {
      final String[] testExclusion = {"src/test/"};
      return pathExlcudes == null ? testExclusion : ArrayUtils.addAll(pathExlcudes, testExclusion);
    }
    return pathExlcudes;
  }

  private File readSarifFile(String sarifInputFile) throws Exception {
    if (StringUtils.isBlank(sarifInputFile)) {
      throw new FileNotFoundException("No Sarif file provided. " + ERR_FILE_SUFFIX);
    }

    LOGGER.fine("read " + sarifInputFile);
    final File result = new File(sarifInputFile);

    if (!result.isFile()) {
      throw new FileNotFoundException(String.format("Specified path is not a valid file: '%s'. %s", sarifInputFile, ERR_FILE_SUFFIX));
    } else if (!result.canRead()) {
      throw new IOException(String.format("Specified file is not readable: '%s'. %s", sarifInputFile, ERR_FILE_SUFFIX));
    }

    return validate(result);
  }

  private File validate(File sarifFile) {
    try {
      final JsonObject rootObject = JsonParser.parseReader(new FileReader(sarifInputFile)).getAsJsonObject();
      if (!rootObject.has("$schema")) {
        throw new RuntimeException(String.format("$schema not found in root object - provided file %s does not seem to be a valid sarif file", sarifFile.getName()));
      }
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
    return sarifFile;
  }
}
