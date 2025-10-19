package apm;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Global {

    public static final String CONFIG_FILE = "apmcreator-config.properties";
    public static final Path configPath = Paths.get(System.getProperty("user.home"), CONFIG_FILE);
    public static final String VERSION = "1.2";
}
