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
