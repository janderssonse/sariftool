package se.janderssonse.sarifconvert;

import java.io.InputStream;
import java.util.logging.ConsoleHandler;
import java.util.logging.LogManager;

import se.janderssonse.sarifconvert.cli.SarifConvertCLI;

public class JavaLoggingConf {

    static {
        try {
            InputStream stream = SarifConvertCLI.class.getClassLoader()
                    .getResourceAsStream("logging.properties");
            LogManager.getLogManager().readConfiguration(stream);

            //GRAALVM throws java.home err for native image, TO-DO
            /*var cmdLineVal = System.getProperty("java.util.logging.loglevel");
            LogManager.getLogManager().updateConfiguration(
                    (key) -> (oldVal, newVal) -> cmdLineVal == null ? oldVal : setLoggingLevel(key, cmdLineVal, oldVal));*/
        } catch (Exception ex) {
            ex.printStackTrace();

        }

    }

    private static String setLoggingLevel(String key, String newVal, String oldVal) {
        if (key.equals(ConsoleHandler.class.getName() + ".level")
                || key.equals(".level")) {
            return newVal;
        }
        return oldVal;
    }
}
