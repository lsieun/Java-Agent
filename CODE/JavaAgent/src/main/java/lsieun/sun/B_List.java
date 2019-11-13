package lsieun.sun;

import java.util.List;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

public class B_List {
    public static void main(String[] args) {
        List<VirtualMachineDescriptor> list = VirtualMachine.list();
        for (VirtualMachineDescriptor desc : list) {
            System.out.println(desc.id());
            System.out.println(desc.displayName());
//            System.out.println(desc.toString());
            System.out.println();
//            System.out.println(desc);
        }
    }
}
