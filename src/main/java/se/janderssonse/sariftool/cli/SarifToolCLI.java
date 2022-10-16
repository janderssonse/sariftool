// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0
package se.janderssonse.sariftool.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi.Style;
import picocli.CommandLine.Help.ColorScheme;

@Command(name = "sariftool", subcommands = { ConvertCommand.class,
        CommandLine.HelpCommand.class }, mixinStandardHelpOptions = true, version = "0.0.1", description = "SARIF tool in early phase", sortOptions = false)
public class SarifToolCLI {

    public static void main(final String... args) {
        int exitCode = new CommandLine(new SarifToolCLI()).execute(args);
        System.exit(exitCode);
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
