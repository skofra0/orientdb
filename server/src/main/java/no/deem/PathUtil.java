package no.deem;

import java.nio.file.Path;
import java.nio.file.Paths;
import com.orientechnologies.orient.server.config.OServerConfiguration;

public enum PathUtil {
    UTIL;

    private static final String ORIENTDB_HOME = "ORIENTDB_HOME";

    public static String toUrlPath(String path) {
        return path.indexOf('\\') < 0 ? path : path.replace('\\', '/');
    }

    public static String toUrlPath(Path path) {
        return toUrlPath(path.toString());
    }

    public static String setOrientdbConfigFileIfMissing() {
        if (isEmpty(System.getProperty(OServerConfiguration.PROPERTY_CONFIG_FILE)) && !isEmpty(System.getProperty(ORIENTDB_HOME))) {
            String orientdbHome = System.getProperty(ORIENTDB_HOME).replace("\"", "");
            Path config = Paths.get(orientdbHome, OServerConfiguration.DEFAULT_CONFIG_FILE).normalize().toAbsolutePath();
            System.setProperty(OServerConfiguration.PROPERTY_CONFIG_FILE, toUrlPath(config));
        }
        return System.getProperty(OServerConfiguration.PROPERTY_CONFIG_FILE, "");
    }

    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

}
