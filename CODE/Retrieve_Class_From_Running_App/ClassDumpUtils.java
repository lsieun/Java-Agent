import java.io.File;
import java.io.FileOutputStream;
import java.util.regex.Pattern;

public class ClassDumpUtils {
    // directory where we would write .class files
    private static String dumpDir;
    // classes with name matching this pattern will be dumped
    private static Pattern classes;

    // parse agent args of the form arg1=value1,arg2=value2
    public static void parseArgs(String agentArgs) {
        if (agentArgs != null) {
            String[] args = agentArgs.split(",");
            for (String arg : args) {
                String[] tmp = arg.split("=");
                if (tmp.length == 2) {
                    String name = tmp[0];
                    String value = tmp[1];
                    if (name.equals("dumpDir")) {
                        dumpDir = value;
                    } else if (name.equals("classes")) {
                        classes = Pattern.compile(value);
                    }
                }
            }
        }
        if (dumpDir == null) {
            dumpDir = ".";
        }
        if (classes == null) {
            classes = Pattern.compile(".*");
        }
    }

    public static boolean isCandidate(String className) {
        // ignore array classes
        if (className.charAt(0) == '[') {
            return false;
        }
        // convert the class name to external name
        className = className.replace('/', '.');
        // check for name pattern match
        return classes.matcher(className).matches();
    }

    public static void dumpClass(String className, byte[] classBuf) {
        try {
            // create package directories if needed
            className = className.replace("/", File.separator);
            StringBuilder buf = new StringBuilder();
            buf.append(dumpDir);
            buf.append(File.separatorChar);
            int index = className.lastIndexOf(File.separatorChar);
            if (index != -1) {
                buf.append(className.substring(0, index));
            }
            String dir = buf.toString();
            new File(dir).mkdirs();
            // write .class file
            String fileName = dumpDir +
                    File.separator + className + ".class";
            FileOutputStream fos = new FileOutputStream(fileName);
            fos.write(classBuf);
            fos.close();
        } catch (Exception exp) {
            exp.printStackTrace();
        }
    }

}
