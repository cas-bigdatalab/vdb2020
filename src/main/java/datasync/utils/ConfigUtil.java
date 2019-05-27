package datasync.utils;

import java.io.*;
import java.util.Properties;

public class ConfigUtil {
    public static String getConfigItem(String configFilePath, String key) {
        String value = null;
        try {
            Properties properties = new Properties();
            properties.load(new InputStreamReader(new FileInputStream(configFilePath)));
            value = properties.getProperty(key);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return value;
    }

    public static void setConfigItem(String configFilePath, String key, String value) {
        try {
            Properties properties = new Properties();

            properties.load(new FileInputStream(configFilePath));
            properties.setProperty(key, value);

            properties.store(new FileOutputStream(configFilePath), "");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
