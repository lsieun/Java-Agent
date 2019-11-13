package lsieun.agent;

import lsieun.agent.transformer.MethodArgsTransformer;
import lsieun.agent.utils.LogUtils;
import lsieun.agent.utils.PropertyUtils;
import lsieun.agent.utils.RegexUtils;

import java.lang.instrument.Instrumentation;

public class Main {
    public static void premain(String agentArgs, Instrumentation inst) {
        LogUtils.log("agentArgs = " + agentArgs);

        // 读取配置文件
        boolean flag = PropertyUtils.readProperties(agentArgs);

        if (flag) {
            inst.addTransformer(new MethodArgsTransformer(), false);
        }
    }

    public static void main(String[] args) {
        usage();
    }

    private static void usage() {
        LogUtils.log("Usage:");
        LogUtils.log("    java -javaagent:method-args.jar=<property file> <ClassName>");
        LogUtils.log("Example:");
        LogUtils.log("    java -javaagent:method-args.jar=agent.properties HelloWorld");
    }
}
