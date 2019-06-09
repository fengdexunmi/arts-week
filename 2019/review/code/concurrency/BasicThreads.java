/**
 * BasicThreads
 */
public class BasicThreads {
    public static void main(String[] args) {
        Thread t = new Thread(new LiftOff());
        t.start();
        for (int i = 0; i < 1000; i++) {
            System.out.println(i + " Waiting for LiftOff");
        }
    }
}