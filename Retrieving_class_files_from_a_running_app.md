# Retrieving .class files from a running app

Sometimes it is better to dump `.class` files of generated/modified classes for off-line debugging - for example, we may want to view such classes using tools like [jclasslib](https://github.com/ingokegel/jclasslib).

The solution below uses attach-on-demand facility in JDK 6.

File: `ClassDumpAgent.java`

```java
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a java.lang.instrument agent to dump .class files
 * from a running Java application.
 */
public class ClassDumpAgent {

    public static void premain(String agentArgs, Instrumentation inst) {
        agentmain(agentArgs, inst);
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        ClassDumpUtils.parseArgs(agentArgs);
        inst.addTransformer(new ClassDumpTransformer(), true);
        // by the time we are attached, the classes to be
        // dumped may have been loaded already. So, check
        // for candidates in the loaded classes.
        Class[] classes = inst.getAllLoadedClasses();
        List<Class> candidates = new ArrayList<>();
        for (Class c : classes) {
            if (inst.isModifiableClass(c) && ClassDumpUtils.isCandidate(c.getName())) {
                candidates.add(c);
            }
        }
        try {
            // if we have matching candidates, then
            // retransform those classes so that we
            // will get callback to transform.
            if (!candidates.isEmpty()) {
                inst.retransformClasses(candidates.toArray(new Class[0]));
            }
        } catch (UnmodifiableClassException uce) {
        }
    }

}
```

File: `ClassDumpTransformer.java`

```java
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class ClassDumpTransformer implements ClassFileTransformer {

    public byte[] transform(ClassLoader loader, String className,
                            Class redefinedClass, ProtectionDomain protDomain,
                            byte[] classBytes) {
        // check and dump .class file
        if (ClassDumpUtils.isCandidate(className)) {
            ClassDumpUtils.dumpClass(className, classBytes);
        }

        // we don't mess with .class file, just
        // return null
        return null;
    }

}
```

File: `ClassDumpUtils.java`

```java
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
```

File: `manifest.mf`

```txt
Premain-Class: ClassDumpAgent
Agent-Class: ClassDumpAgent
Can-Redefine-Classes: true
Can-Retransform-Classes: true
```

File: `Attach.java`

```java
import com.sun.tools.attach.VirtualMachine;

/**
 * Simple attach-on-demand client tool that
 * loads the given agent into the given Java process.
 */
public class Attach {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("usage: java Attach <pid> <agent-jar-full-path> [<agent-args>]");
            System.exit(1);
        }
        // JVM is identified by process id (pid).
        VirtualMachine vm = VirtualMachine.attach(args[0]);
        String agentArgs = (args.length > 2)? args[2] : null;
        // load a specified agent onto the JVM
        vm.loadAgent(args[1], agentArgs);
    }
}
```

Steps to build class dumper:

- `javac ClassDump*.java`
- `jar -cvfm classdumper.jar manifest.mf ClassDump*.class`
- `javac -cp $JAVA_HOME/lib/tools.jar Attach.java`

Steps to run class dumper:

- start your target process
- find the process id of your process using "jps" tool, `jps -l`
- `java -cp $JAVA_HOME/lib/tools.jar:. Attach <pid> <full-path-of-classdumper.jar> dumpDir=<dir>,classes=<name-pattern>`

```bash
## 生成到程序运行的目录
java -cp $JAVA_HOME/lib/tools.jar:. Attach 15780 /home/liusen/Workspace/git-repo/Java-Agent/CODE/Retrieve_Class_From_Running_App/classdumper.jar dumpDir=.,classes=com\\.lsieun\\.tank\\.util\\.UUIDGenerator

## 生成到${PWD}目录
java -cp $JAVA_HOME/lib/tools.jar:. Attach 15780 /home/liusen/Workspace/git-repo/Java-Agent/CODE/Retrieve_Class_From_Running_App/classdumper.jar dumpDir=${PWD},classes=com\\.lsieun\\.tank\\.util.*
```

The above command will dump all classes matching the given name (regex) pattern into the given directory. The default dump directory is the current working directory (of the target application!). The default pattern is "`.*`" i.e., match all classes loaded/will be loaded in the target application.

Now, how about a nice GUI that

- shows all the Java processes
- lets you can choose dump directory
- Optionally, shows all the classes of a selected application

As usual, that is left as an exercise to the reader :-)

## Reference

- [Retrieving .class files from a running app](https://blogs.oracle.com/sundararajan/retrieving-class-files-from-a-running-app)
