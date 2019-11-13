package lsieun;

import lsieun.transformer.InfoTransformer;

import java.lang.instrument.Instrumentation;

public class Main {
    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("agentArgs = " + agentArgs);
        inst.addTransformer(new InfoTransformer(), false);
    }

}
