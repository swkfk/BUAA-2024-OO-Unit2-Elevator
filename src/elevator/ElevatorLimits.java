package elevator;

public class ElevatorLimits {
    private int maxPassenger = 6;
    public static final int ELEVATOR_COUNT = 6;
    private int minFloor = 1;
    private int maxFloor = 11;
    private long moveDurationMs = 400;
    public static final long OPEN_DURATION_MS = 200;
    public static final long CLOSE_DURATION_MS = 200;
    public static final long OPENED_DURATION_MS = OPEN_DURATION_MS + CLOSE_DURATION_MS;
    public static final long RESET_DURATION_MS = 1200;

    public ElevatorLimits() {
    }

    public ElevatorLimits(int maxPassenger, long moveDurationMs) {
        // System.out.println(moveDurationMs);  // To check the float precision
        this.maxPassenger = maxPassenger;
        this.moveDurationMs = moveDurationMs;
    }

    public void setLimitFloor(int minFloor, int maxFloor) {
        this.minFloor = minFloor;
        this.maxFloor = maxFloor;
    }

    public int getMinFloor() {
        return minFloor;
    }

    public int getMaxFloor() {
        return maxFloor;
    }

    public int getMaxPassenger() {
        return maxPassenger;
    }

    public long getMoveDurationMs() {
        return moveDurationMs;
    }
}
