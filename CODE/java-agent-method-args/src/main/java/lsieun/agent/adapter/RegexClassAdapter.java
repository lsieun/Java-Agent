package lsieun.agent.adapter;

import jdk.internal.org.objectweb.asm.ClassVisitor;
import lsieun.agent.utils.RegexUtils;

import java.util.ArrayList;
import java.util.List;

import static jdk.internal.org.objectweb.asm.Opcodes.ACC_INTERFACE;
import static jdk.internal.org.objectweb.asm.Opcodes.ASM5;

public class RegexClassAdapter extends ClassVisitor {
    public static final int ASM_VERSION = ASM5;

    // 开始前，传入的参数
    public final String[] includes;
    public final String[] excludes;

    // 过程中，记录的参数
    public String internalName;
    public boolean isInterface;

    // 结束后，输出的参数
    public boolean gotcha = false;
    public final List<Result> resultList;

    public RegexClassAdapter(ClassVisitor classVisitor, String[] includes, String[] excludes) {
        super(ASM_VERSION, classVisitor);
        this.includes = includes;
        this.excludes = excludes;
        this.resultList = new ArrayList();
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        internalName = name;
        isInterface = (access & ACC_INTERFACE) != 0;

        super.visit(version, access, name, signature, superName, interfaces);
    }

    public boolean isTargetMember(String name, String descriptor) {
        boolean flag = isAppropriate(name, descriptor);
        if (flag) {
            this.gotcha = true;
            Result result = new Result(internalName);
            result.add(name, descriptor);
            resultList.add(result);
            return true;
        } else {
            return false;
        }
    }

    public boolean isAppropriate(String name, String descriptor) {
        String name_desc = String.format("%s:%s", name, descriptor);
        return isAppropriate(name_desc);
    }

    public boolean isAppropriate(String item) {
        if (RegexUtils.matches(item, this.excludes, false)) {
            return false;
        } else {
            return RegexUtils.matches(item, this.includes, true);
        }
    }
}
