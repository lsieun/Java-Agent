package lsieun.transformer;

import lsieun.asm.adapter.enhance.MethodInfoAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class InfoTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader,
                            String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) throws IllegalClassFormatException {
        if ("java/lang/invoke/LambdaMetafactory".equals(className)) {
            System.out.println("Find");
            ClassReader cr = new ClassReader(classfileBuffer);
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            MethodInfoAdapter mia = new MethodInfoAdapter(cw, new String[] {
                    "^metafactory:\\(Ljava/lang/invoke/MethodHandles\\$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;\\)Ljava/lang/invoke/CallSite;$"
            }, null);

            cr.accept(mia, 0);

            byte[] bytes = cw.toByteArray();
            return bytes;
        }
        return null;
    }
}
