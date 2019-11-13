package lsieun.agent.adapter;

import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.MethodVisitor;

import static jdk.internal.org.objectweb.asm.Opcodes.ACC_ABSTRACT;

public abstract class MethodEnhancedAdapter extends RegexClassAdapter {
    public MethodEnhancedAdapter(ClassVisitor classVisitor, String[] includes, String[] excludes) {
        super(classVisitor, includes, excludes);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

        // （1） 接口，不处理
        if (isInterface) {
            return mv;
        }

        //（2）抽象方法，不处理
        boolean isAbstractMethod = (access & ACC_ABSTRACT) != 0;
        if (isAbstractMethod) {
            return mv;
        }

        //（3）不符合正则表达式，不处理
        boolean isTargetMethod = isTargetMember(name, descriptor);
        if (!isTargetMethod) {
            return mv;
        }

        //（4）开始处理
        return enhanceMethodVisitor(mv, access, name, descriptor, signature, exceptions);
    }


    protected abstract MethodVisitor enhanceMethodVisitor(MethodVisitor mv, int access, String name, String descriptor, String signature, String[] exceptions);

}
