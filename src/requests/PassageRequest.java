package requests;

import com.oocourse.elevator3.PersonRequest;
import elevator.ElevatorDirection;

public class PassageRequest extends BaseRequest {
    private int fromFloor;
    private final int toFloor;
    private final int personId;
    private int elevatorId;
    private final ElevatorDirection direction;

    public PassageRequest(PersonRequest request) {
        this.fromFloor = request.getFromFloor();
        this.toFloor = request.getToFloor();
        this.personId = request.getPersonId();
        this.elevatorId = -1;
        this.direction = toFloor > fromFloor ? ElevatorDirection.UP : ElevatorDirection.DOWN;
    }

    public int getFromFloor() {
        return fromFloor;
    }

    public int getToFloor() {
        return toFloor;
    }

    public int getPersonId() {
        return personId;
    }

    public int getElevatorId() {
        return elevatorId;
    }

    public void setElevatorId(int elevatorId) {
        this.elevatorId = elevatorId;
    }

    public void setFromFloor(int fromFloor) {
        this.fromFloor = fromFloor;
    }

    public boolean sameDirection(ElevatorDirection direction) {
        return this.direction == direction;
    }
}
