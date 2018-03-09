package tlou;

public class Main {

    public static void main(String[] args) {
        System.out.println("hello");
        TimeLogManager tlm = new TimeLogManager();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        tlm.addNow();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        tlm.addNow();
        tlm.writeLog("/tmp/asd");
        tlm.readLog("/tmp/asd");
    }
}
