package no.nexus;

import java.io.File;

import com.orientechnologies.common.parser.OSystemVariableResolver;
import com.orientechnologies.orient.server.OServerMain;
import com.orientechnologies.orient.server.OServerShutdownMain;
import com.orientechnologies.orient.server.config.OServerConfiguration;
import com.orientechnologies.orient.server.config.OServerConfigurationLoaderXml;
import com.orientechnologies.orient.server.config.OServerUserConfiguration;

public class OrientDbService {
    private static boolean stop = false;
    private static boolean readPasswordFromConfig = false;

    public static final String DEFAULT_ROOT_USER = "root";

    public static void start(String[] args) throws Exception {

        PathUtil.setOrientdbConfigFileIfMissing();
        
        OServerMain.main(null);
        System.out.println("OrientDbService - start");
        while (!stop) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
    }

    public static void stop(String[] args) throws Exception {
        System.out.println("OrientDbService - stop");

        String rootPassword = "admin";
        String rootUser = OrientDbService.DEFAULT_ROOT_USER;
        String iServerPorts = "2424-2430";
        String iServerAddress = "localhost";

        stop = true;

        if (readPasswordFromConfig) {
            try {
                OServerConfiguration configuration = (new OServerConfigurationLoaderXml(OServerConfiguration.class, getConfigFile())).load();
                if (configuration.users != null && configuration.users.length > 0) {
                    for (OServerUserConfiguration u : configuration.users) {
                        if (u.name.equals(OrientDbService.DEFAULT_ROOT_USER)) {
                            // FOUND
                            rootPassword = u.password;
                            break;
                        }
                    }
                }
            } catch (Exception e) {

            }
        }
        // 2.2
        new OServerShutdownMain(iServerAddress, iServerPorts, rootUser, rootPassword).connect(5000);
    }

    public static File getConfigFile() {
        String config = OServerConfiguration.DEFAULT_CONFIG_FILE;
        if (System.getProperty(OServerConfiguration.PROPERTY_CONFIG_FILE) != null) {
            config = System.getProperty(OServerConfiguration.PROPERTY_CONFIG_FILE);
        }
        return new File(OSystemVariableResolver.resolveSystemVariables(config));
    }

    public static void main(String[] args) throws Exception {
        if ("start".equals(args[0])) {
            start(args);
        } else if ("stop".equals(args[0])) {
            stop(args);
        }
    }
}
