// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0
package se.janderssonse.sariftool.conf;

import java.io.InputStream;
import java.util.logging.LogManager;

import se.janderssonse.sariftool.cli.SarifToolCLI;

public class JavaLoggingConf {

    static {
        try {
            InputStream stream = SarifToolCLI.class.getClassLoader()
                    .getResourceAsStream("logging.properties");
            LogManager.getLogManager().readConfiguration(stream);

            // GRAALVM throws java.home err for native image, TO-DO
            /*
             * var cmdLineVal = System.getProperty("java.util.logging.loglevel");
             * LogManager.getLogManager().updateConfiguration(
             * (key) -> (oldVal, newVal) -> cmdLineVal == null ? oldVal :
             * setLoggingLevel(key, cmdLineVal, oldVal));
             */
        } catch (Exception ex) {
            ex.printStackTrace();

        }

    }
    /*
     * private static String setLoggingLevel(final String key, final String newVal,
     * final String oldVal) {
     * if (key.equals(ConsoleHandler.class.getName() + ".level")
     * || key.equals(".level")) {
     * return newVal;
     * }
     * return oldVal;
     * }
     */
}
