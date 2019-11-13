package lsieun.sun;

import java.io.IOException;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

public class A_Attach {
    public static void main(String[] args) {
        try {
            VirtualMachine vm = VirtualMachine.attach("19270");
            System.out.println(vm.id());
            System.out.println("=== === ===" + System.lineSeparator());

            System.out.println(vm.getSystemProperties());
            System.out.println(vm.getAgentProperties());
            System.out.println(vm.getClass());
        } catch (AttachNotSupportedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
