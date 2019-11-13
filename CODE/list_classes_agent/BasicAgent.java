import java.lang.instrument.Instrumentation;

public class BasicAgent {
    public static void premain(String agentArgs, Instrumentation inst) {
        inst.addTransformer(new ClassListingTransformer());
    }
}
