package lsieun.agent.transformer;

import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassWriter;
import lsieun.agent.adapter.MethodArgsAdapter;
import lsieun.agent.adapter.NameAndDesc;
import lsieun.agent.adapter.Result;
import lsieun.agent.utils.LogUtils;
import lsieun.agent.utils.PropertyUtils;
import lsieun.agent.utils.RegexUtils;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class MethodArgsTransformer implements ClassFileTransformer {
    private static int display_order = 1;
    private String internal_name_regex;
    private String[] includes;
    private String[] excludes;

    public MethodArgsTransformer() {
        internal_name_regex = PropertyUtils.getInternalNameRegex();
        includes = PropertyUtils.getIncludes();
        excludes = PropertyUtils.getExcludes();
    }

    @Override
    public byte[] transform(
            ClassLoader loader,
            String className,
            Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain,
            byte[] classfileBuffer) throws IllegalClassFormatException {

//        if (className != null) {
//            LogUtils.log(className);
//        }
        if (className != null && RegexUtils.matches(className, internal_name_regex)) {
            LogUtils.log("Find: " + className);
            ClassReader cr = new ClassReader(classfileBuffer);
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            MethodArgsAdapter argsAdapter = new MethodArgsAdapter(cw, includes, excludes);
            cr.accept(argsAdapter, 0);

            if (argsAdapter.gotcha) {
                int size = argsAdapter.resultList.size();
                for (int i = 0; i < size; i++) {
                    Result result = argsAdapter.resultList.get(i);
                    LogUtils.log(String.format("(%s) %s", display_order++, result.className));
                    for (int j = 0; j < result.list.size(); j++) {
                        NameAndDesc item = result.list.get(j);
                        LogUtils.log(item.toString());
                    }
                }
                byte[] bytes = cw.toByteArray();
                return bytes;
            }

        }
        return null;
    }
}
