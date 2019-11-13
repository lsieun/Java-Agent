package lsieun.agent.utils;

import java.io.*;
import java.util.Properties;

public class PropertyUtils {
    private static final String DEFAULT_CONFIG_FILE = "agent.properties";
    private static final String INTERNAL_NAME_REGEX = "internal.name.regex";
    private static final String METHOD_SIGNATURE_INCLUDES_REGEX = "method.signature.includes.regex";
    private static final String METHOD_SIGNATURE_EXCLUDES_REGEX = "method.signature.excludes.regex";
    private static final Properties props = new Properties();

    public static boolean readProperties(final String filepath) {
        final String configPath = processFilePath(filepath);

        LogUtils.log("READ Config File: " + configPath);
        File file = new File(configPath);
        if (!file.exists()) {
            writeProperties();
            LogUtils.log(System.lineSeparator());
            LogUtils.log("You can do one of the following:");
            LogUtils.log("    (1) Modify File: " + configPath);
            LogUtils.log("    (2) java -javaagent:method-args.jar=<my.properties> <ClassName>");
            return false;
        }

        try {
            props.load(new FileInputStream(configPath));
            if (props.containsKey(INTERNAL_NAME_REGEX) &&
                    props.containsKey(METHOD_SIGNATURE_INCLUDES_REGEX) &&
                    props.containsKey(METHOD_SIGNATURE_EXCLUDES_REGEX)) {
                return true;
            }
            return false;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static String processFilePath(final String filepath) {
        if (filepath == null || "".equals(filepath.trim())) {
            return getDefaultConfigFile();
        }
        return filepath.trim();
    }

    public static String getDefaultConfigFile() {
        return System.getProperty("user.dir") + File.separator + DEFAULT_CONFIG_FILE;
    }

    public static void writeProperties() {
        Properties ps = new Properties();
        ps.setProperty(INTERNAL_NAME_REGEX, "^java/lang/invoke/LambdaMetafactory$");
        ps.setProperty(METHOD_SIGNATURE_INCLUDES_REGEX, "^metafactory:\\(Ljava/lang/invoke/MethodHandles\\$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;\\)Ljava/lang/invoke/CallSite;$");
        ps.setProperty(METHOD_SIGNATURE_EXCLUDES_REGEX, "^\\w+:$");

        String filepath = getDefaultConfigFile();
        try (OutputStream out = new FileOutputStream(filepath)) {
            ps.store(out, "Note: Use Java Regular Expression");
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static String getProperty(String key) {
        String value = props.getProperty(key);
        System.out.println(key + " = " + value);
        return value;
    }

    public static String getInternalNameRegex() {
        String property = getProperty(INTERNAL_NAME_REGEX);
        if (property == null || "".equals(property)) {
            return "^.+$";
        }
        return property;
    }

    public static String[] getIncludes() {
        String property = getProperty(METHOD_SIGNATURE_INCLUDES_REGEX);
        if (property == null || "".equals(property)) {
            return new String[]{"^\\w+:$"};
        }
        return new String[]{property};
    }

    public static String[] getExcludes() {
        String property = getProperty(METHOD_SIGNATURE_EXCLUDES_REGEX);
        if (property == null || "".equals(property)) {
            return null;
        }
        return new String[]{property};
    }
}
