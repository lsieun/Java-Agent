package lsieun.agent.utils;

import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Type;

import static jdk.internal.org.objectweb.asm.Opcodes.*;

public class CodeUtils {
    public static final String PRINT_STRING = "(Ljava/lang/String;)V";

    public static void printMethodName(MethodVisitor mv, String owner, String methodName, String methodDesc) {
        String format = "%s!%s:%s";
        String[] args = new String[]{owner, methodName, methodDesc};
        printMessage(mv, format, args);
    }

    public static void printMethodArguments(MethodVisitor mv, int access, String methodDesc) {
        if (mv != null) {
            Type[] argumentTypes = Type.getArgumentTypes(methodDesc);
            if (argumentTypes != null && argumentTypes.length > 0) {

                int slot_index = 1; // 如果是non-static method，第1个参数位于索引为1的slot上
                boolean isStaticMethod = (access & ACC_STATIC) != 0;
                if (isStaticMethod) {
                    slot_index = 0; // 如果是static method，第1个参数位于索引为0的slot上
                }

                String format = "|%s| %s: %s";

                for (int i = 0; i < argumentTypes.length; i++) {
                    Type t = argumentTypes[i];
                    String desc = t.getDescriptor();
                    if (t.getSort() >= Type.BOOLEAN && t.getSort() <= Type.DOUBLE) {
                        if (t.getSort() >= Type.BOOLEAN && t.getSort() <= Type.INT) {
                            mv.visitVarInsn(ILOAD, slot_index);
                            CodeBlock.int2Integer(mv);
                        } else if (t.getSort() == Type.FLOAT) {
                            mv.visitVarInsn(FLOAD, slot_index);
                            CodeBlock.float2Float(mv);
                        } else if (t.getSort() == Type.LONG) {
                            mv.visitVarInsn(LLOAD, slot_index);
                            CodeBlock.long2Long(mv);
                        } else if (t.getSort() == Type.DOUBLE) {
                            mv.visitVarInsn(DLOAD, slot_index);
                            CodeBlock.double2Double(mv);
                        }
                    } else if (t.getSort() == Type.ARRAY || t.getSort() == Type.OBJECT) {
                        mv.visitVarInsn(ALOAD, slot_index);
                    } else {
                        throw new RuntimeException("Unknown Type: " + t);
                    }
                    slot_index += t.getSize();

                    String display_order = String.format("%03d", (i + 1));
                    printStackObjectValue(mv, format, new String[]{display_order, desc});
                }
            }
        }
    }

    public static void printMethodReturnValue(MethodVisitor mv, int opcode, String methodDesc) {
        if (mv != null) {
            if (opcode >= IRETURN && opcode <= RETURN) {
                Type t = Type.getReturnType(methodDesc);
                Type[] argumentTypes = Type.getArgumentTypes(methodDesc);
                String display_order = String.format("%03d", (argumentTypes == null ? 1 : argumentTypes.length + 1));

                if (t.getSort() == Type.VOID) {
                    printMessage(mv, "return VOID");
                } else if (t.getSort() >= Type.BOOLEAN && t.getSort() <= Type.OBJECT) {
                    String format = "|%s| return %s: %s";
                    String desc = t.getDescriptor();

                    dupStackValue(mv, t);
                    CodeUtils.printStackObjectValue(mv, format, new String[]{display_order, desc});
                } else {
                    throw new RuntimeException("Unknown Type: " + t);
                }

            } else if (opcode == ATHROW) {
                printMessage(mv, "=========AThrow=========" + methodDesc);
                //printStackTrace(mv);
            }

        }
    }

    public static void printStackTrace(MethodVisitor mv) {
        if (mv != null) {
            printMessage(mv, "=== === ===>>> Stack Trace");
            mv.visitTypeInsn(NEW, "java/lang/Exception");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Exception", "<init>", "()V", false);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Exception", "printStackTrace", "()V", false);
            printMessage(mv, "<<<=== === === Stack Trace");
        }
    }

    public static void printStackObjectValue(MethodVisitor mv, String format, String[] args) {
        // 先验证参数，是否正确
        if (format == null || format.trim().isEmpty()) return;
        if (args == null || args.length < 1) return;

        // 再实现功能，要做什么
        if (mv != null) {
            mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            mv.visitInsn(SWAP);
            mv.visitLdcInsn(format);
            mv.visitInsn(SWAP);

            mv.visitIntInsn(BIPUSH, args.length + 1);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");

            storeStringArray(mv, args);

            mv.visitInsn(DUP_X1);
            mv.visitInsn(SWAP);
            mv.visitIntInsn(BIPUSH, args.length);
            mv.visitInsn(SWAP);
            mv.visitInsn(AASTORE);


            mv.visitMethodInsn(INVOKESTATIC, "java/lang/String", "format", "(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;", false);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", PRINT_STRING, false);
        }
    }

    public static void printMessage(MethodVisitor mv, String message) {
        if (mv != null) {
            mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            mv.visitLdcInsn(message);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", PRINT_STRING, false);
        }
    }

    public static void printMessage(MethodVisitor mv, String format, String[] args) {
        // 先验证参数，是否正确
        if (format == null || format.trim().isEmpty()) return;
        if (args == null || args.length < 1) return;

        // 再实现功能，要做什么
        if (mv != null) {
            mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            mv.visitLdcInsn(format);

            mv.visitIntInsn(BIPUSH, args.length);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/String");

            storeStringArray(mv, args);

            mv.visitMethodInsn(INVOKESTATIC, "java/lang/String", "format", "(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;", false);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", PRINT_STRING, false);
        }
    }

    private static void storeStringArray(MethodVisitor mv, String[] args) {
        for (int i = 0; i < args.length; i++) {
            mv.visitInsn(DUP);
            mv.visitIntInsn(BIPUSH, i);
            mv.visitLdcInsn(args[i]);
            mv.visitInsn(AASTORE);
        }
    }

    public static void dupStackValue(MethodVisitor mv, Type t) {
        if (t.getSort() >= Type.BOOLEAN && t.getSort() <= Type.INT) { // boolean
            mv.visitInsn(DUP);
            CodeBlock.int2Integer(mv);
        } else if (t.getSort() == Type.FLOAT) {
            mv.visitInsn(DUP);
            CodeBlock.float2Float(mv);
        } else if (t.getSort() == Type.LONG) {
            mv.visitInsn(DUP2);
            CodeBlock.long2Long(mv);
        } else if (t.getSort() == Type.DOUBLE) {
            mv.visitInsn(DUP2);
            CodeBlock.double2Double(mv);
        } else if (t.getSort() == Type.ARRAY || t.getSort() == Type.OBJECT) {
            mv.visitInsn(DUP);
        } else {
            mv.visitInsn(DUP);
        }
    }
}
