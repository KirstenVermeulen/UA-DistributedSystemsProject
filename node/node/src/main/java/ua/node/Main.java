package ua.node;

public class Main {
    public static void main(String[] args)  {

        try {
            Node node = new Node();
            Thread.sleep(2000);
            node.run();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
