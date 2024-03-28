package requests;

import com.oocourse.elevator1.PersonRequest;
import elevator.ElevatorDirection;

public class PassageRequest {
    private final int fromFloor;
    private final int toFloor;
    private final int personId;
    private final int elevatorId;
    private final ElevatorDirection direction;

    public PassageRequest(PersonRequest request) {
        this.fromFloor = request.getFromFloor();
        this.toFloor = request.getToFloor();
        this.personId = request.getPersonId();
        this.elevatorId = request.getElevatorId();
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

    public boolean sameDirection(ElevatorDirection direction) {
        return this.direction == direction;
    }
}
