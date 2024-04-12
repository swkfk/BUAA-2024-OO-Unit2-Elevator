package controller;

public class GlobalCounter {
    private static int passengerCount = 0;

    public static synchronized void increase() {
        passengerCount++;
        GlobalCounter.class.notify();
    }

    public static synchronized void decrease() {
        passengerCount--;
        GlobalCounter.class.notify();
    }

    public static synchronized boolean zero() {
        return passengerCount == 0;
    }

    public static synchronized void rawNotify() {
        GlobalCounter.class.notify();
    }
}
