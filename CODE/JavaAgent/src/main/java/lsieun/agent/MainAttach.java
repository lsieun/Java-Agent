package lsieun.agent;

import java.io.File;
import java.util.List;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

public class MainAttach {
    public static void main(String[] args) throws Exception {
        List<VirtualMachineDescriptor> vmds = VirtualMachine.list();
        for (VirtualMachineDescriptor vmd : vmds) {
            if (vmd.displayName().equals("lsieun.agent.Main")) {
                VirtualMachine vm = VirtualMachine.attach(vmd.id());

                String agent = vm.getSystemProperties()
                        .getProperty("user.home") + File.separator +
                        "lib" + File.separator + "asm-agent-1.0-SNAPSHOT.jar";

                vm.loadAgent(agent);
                // Detach.
                vm.detach();
            }
        }


    }
}
