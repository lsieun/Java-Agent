package lsieun.agent;

public class Main {
    public static void main(String[] args) {
        while(true) {
            int num = HelloWorld.getNumber();
            System.out.println("Current Num = " + num);

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
