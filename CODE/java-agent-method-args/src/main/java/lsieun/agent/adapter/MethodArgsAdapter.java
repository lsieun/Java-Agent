package lsieun.agent.adapter;

import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.commons.AdviceAdapter;
import lsieun.agent.utils.CodeUtils;

public class MethodArgsAdapter extends MethodEnhancedAdapter {
    // 开始前，传入参数
    private boolean showMethodName;
    private boolean showMethodArgs;
    private boolean showMethodReturnValue;
    private boolean showStackTrace;

    public MethodArgsAdapter(ClassVisitor classVisitor, String[] includes, String[] excludes) {
        this(classVisitor, includes, excludes, true, true, true, true);
    }

    public MethodArgsAdapter(ClassVisitor classVisitor, String[] includes, String[] excludes,
                             boolean showMethodName,
                             boolean showMethodArgs,
                             boolean showMethodReturnValue,
                             boolean showStackTrace) {
        super(classVisitor, includes, excludes);
        this.showMethodName = showMethodName;
        this.showMethodArgs = showMethodArgs;
        this.showMethodReturnValue = showMethodReturnValue;
        this.showStackTrace = showStackTrace;
    }

    @Override
    protected MethodVisitor enhanceMethodVisitor(MethodVisitor mv, int access, String name, String descriptor, String signature, String[] exceptions) {
        return new MethodInfoVisitor(mv, access, name, descriptor);
    }

    class MethodInfoVisitor extends AdviceAdapter {

        private String methodName;

        public MethodInfoVisitor(MethodVisitor methodVisitor, int access, String name, String descriptor) {
            super(ASM_VERSION, methodVisitor, access, name, descriptor);
            methodName = name;
        }

        @Override
        protected void onMethodEnter() {
            if (mv != null) {
                CodeUtils.printMessage(mv, System.lineSeparator());
                if (showMethodName) {
                    CodeUtils.printMethodName(mv, internalName, methodName, methodDesc);
                }

                if (showMethodArgs) {
                    CodeUtils.printMethodArguments(mv, methodAccess, methodDesc);
                }

                if (showStackTrace) {
                    CodeUtils.printStackTrace(mv);
                }

            }
        }

        @Override
        protected void onMethodExit(int opcode) {
            if (mv != null) {
                if (showMethodReturnValue) {
                    CodeUtils.printMethodReturnValue(mv, opcode, methodDesc);
                }
                CodeUtils.printMessage(mv, System.lineSeparator());
            }
        }
    }
}
