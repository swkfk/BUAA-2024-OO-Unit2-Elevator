package elevator;

public class ElevatorLimits {
    private int maxPassenger = 6;
    public static final int ELEVATOR_COUNT = 6;
    private int minFloor = 1;
    private int maxFloor = 11;
    private int transferFloor = -1;
    private long moveDurationMs = 400;
    private ElevatorDirection zoneDirection = ElevatorDirection.UP;
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

    public ElevatorLimits setLimitFloor(int minFloor, int maxFloor) {
        ElevatorLimits newLimits = new ElevatorLimits(maxPassenger, moveDurationMs);
        newLimits.minFloor = minFloor;
        newLimits.maxFloor = maxFloor;
        if (minFloor == 1) {
            newLimits.transferFloor = maxFloor;
            newLimits.zoneDirection = ElevatorDirection.DOWN;
        } else if (maxFloor == 11) {
            newLimits.transferFloor = minFloor;
            newLimits.zoneDirection = ElevatorDirection.UP;
        }
        return newLimits;
    }

    public int getMinFloor() {
        return minFloor;
    }

    public int getMaxFloor() {
        return maxFloor;
    }

    public int getTransferFloor() {
        return transferFloor;
    }

    public int getMaxPassenger() {
        return maxPassenger;
    }

    public long getMoveDurationMs() {
        return moveDurationMs;
    }

    public ElevatorDirection getZoneDirection() {
        return zoneDirection;
    }
}
