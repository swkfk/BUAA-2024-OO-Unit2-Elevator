package elevator;

public class Elevator {
    private final int elevatorId;
    private ElevatorStatus status;
    private ElevatorDirection direction;
    private int floor;

    public Elevator(int elevatorId) {
        this.elevatorId = elevatorId;
        this.status = ElevatorStatus.IDLE;
        this.direction = ElevatorDirection.UP;
        this.floor = ElevatorLimits.MIN_FLOOR;
    }

    public int getElevatorId() {
        return elevatorId;
    }

    public int getFloor() {
        return floor;
    }
}
