package elevator;

import controller.FormattedPrinter;
import requests.PassageRequest;

import java.util.ArrayList;
import java.util.Iterator;

public class Elevator {
    private final int elevatorId;
    private boolean doorOpen;
    private ElevatorDirection direction;
    private int floor;
    private final ArrayList<PassageRequest> passageRequests;

    private final ArrayList<PassageRequest> onboardRequests;

    public Elevator(int elevatorId) {
        this.elevatorId = elevatorId;
        this.doorOpen = false;
        this.direction = ElevatorDirection.UP;
        this.floor = ElevatorLimits.MIN_FLOOR;
        this.passageRequests = new ArrayList<>();
        this.onboardRequests = new ArrayList<>();
    }

    public boolean canTerminate() {
        return passageRequests.isEmpty() && onboardRequests.isEmpty() && !doorOpen;
    }

    public int getElevatorId() {
        return elevatorId;
    }

    public int getFloor() {
        return floor;
    }

    public ElevatorDirection getDirection() {
        return direction;
    }

    public ArrayList<PassageRequest> getOnboards() {
        return onboardRequests;
    }

    public void addRequest(PassageRequest request) {
        passageRequests.add(request);
    }

    public ArrayList<PassageRequest> getRequests() {
        return passageRequests;
    }

    public void move() {
        if (direction == ElevatorDirection.UP) {
            floor++;
        } else {
            floor--;
        }
        FormattedPrinter.elevatorArrive(this);
    }

    public void moveReversely() {
        reverse();
        move();
    }

    public void reverse() {
        if (direction == ElevatorDirection.UP) {
            direction = ElevatorDirection.DOWN;
        } else {
            direction = ElevatorDirection.UP;
        }
    }

    public boolean isDoorOpen() {
        return doorOpen;
    }

    public void openDoor() {
        FormattedPrinter.elevatorOpen(this);
        doorOpen = true;
        Iterator<PassageRequest> iterator = onboardRequests.iterator();
        while (iterator.hasNext()) {
            PassageRequest request = iterator.next();
            if (request.getToFloor() == floor) {
                FormattedPrinter.passengerLeave(request);
                iterator.remove();
            }
        }
    }

    public void closeDoor() {
        Iterator<PassageRequest> iterator = passageRequests.iterator();
        while (iterator.hasNext()) {
            PassageRequest request = iterator.next();
            if (request.getFromFloor() == floor
                    && onboardRequests.size() < ElevatorLimits.MAX_PASSENGER
                    && request.sameDirection(direction)) {
                FormattedPrinter.passengerEnter(request);
                onboardRequests.add(request);
                iterator.remove();
            }
        }
        doorOpen = false;
        FormattedPrinter.elevatorClose(this);
    }
}
