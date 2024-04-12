package controller;

public class GlobalCounter {
    private static int passengerCount = 0;

    public static synchronized void increase() {
        passengerCount++;
    }

    public static synchronized void decrease() {
        passengerCount--;
    }

    public static synchronized boolean zero() {
        return passengerCount == 0;
    }
}
