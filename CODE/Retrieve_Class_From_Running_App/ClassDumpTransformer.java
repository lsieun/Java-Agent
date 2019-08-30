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
