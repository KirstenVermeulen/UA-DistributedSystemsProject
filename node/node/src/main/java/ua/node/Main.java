package ua.node;

import ua.util.MulticastPublisher;
import ua.util.MulticastReceiver;

public class Main {
    public static void main(String[] args)  {

        MulticastReceiver receiver = new MulticastReceiver();
        receiver.start();
    }
}
