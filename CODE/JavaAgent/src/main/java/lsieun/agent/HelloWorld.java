package lsieun.agent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HelloWorld {
    public static void main(String[] args) {
        String property = System.getProperty("os.family");
        System.out.println(property);
    }

    public static int getNumber() {
        return 33;
    }
}
