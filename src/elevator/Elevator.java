package elevator;

import controller.FormattedPrinter;
import controller.GlobalCounter;
import requests.BaseRequest;
import requests.PassageRequest;
import requests.RequestsQueue;
import requests.ResetRequest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

public class Elevator {
    private final int elevatorId;
    private boolean doorOpen;
    private ElevatorDirection direction;
    private int floor;
    private final ArrayList<PassageRequest> passageRequests;

    private final ArrayList<PassageRequest> onboardRequests;
    private ElevatorLimits limits;
    private String outputName;
    private ReentrantLock lock;

    public Elevator(int elevatorId, String name, ReentrantLock lock) {
        this.elevatorId = elevatorId;
        this.doorOpen = false;
        this.direction = ElevatorDirection.UP;
        this.floor = 1;
        this.passageRequests = new ArrayList<>();
        this.onboardRequests = new ArrayList<>();
        this.limits = new ElevatorLimits();
        this.outputName = name;
        this.lock = lock;
    }

    public void setOutputNameToA() {
        this.outputName += "-A";
    }

    public String getOutputName() {
        return outputName;
    }

    public boolean canTerminate() {
        return passageRequests.isEmpty() && onboardRequests.isEmpty() && !doorOpen;
    }

    public int getElevatorId() {
        return elevatorId;
    }

    public void setLimitFloor(int min, int max, int floor) {
        this.floor = floor;
        this.limits = this.limits.setLimitFloor(min, max);
        direction = limits.getZoneDirection();
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
        int originalFloor = floor;
        if (direction == ElevatorDirection.UP) {
            if (floor + 1 == limits.getTransferFloor()) {
                lock.lock();
            }
            floor++;
        } else {
            if (floor - 1 == limits.getTransferFloor()) {
                lock.lock();
            }
            floor--;
        }
        FormattedPrinter.elevatorArrive(this);
        if (originalFloor == limits.getTransferFloor()) {
            lock.unlock();
        }
    }

    public void moveReversely() {
        reverse();
        ensureDirection();
        move();
    }

    public void reverse() {
        if (direction == ElevatorDirection.UP) {
            direction = ElevatorDirection.DOWN;
        } else {
            direction = ElevatorDirection.UP;
        }
    }

    public void ensureDirection() {
        if (floor == limits.getTransferFloor()) {
            direction = limits.getZoneDirection();
        } else if (floor == limits.getMinFloor()) {
            direction = ElevatorDirection.UP;
        } else if (floor == limits.getMaxFloor()) {
            direction = ElevatorDirection.DOWN;
        }
    }

    public boolean isDoorOpen() {
        return doorOpen;
    }

    public long openDoor(RequestsQueue<BaseRequest> waitQueue) {
        FormattedPrinter.elevatorOpen(this);
        final long t = System.currentTimeMillis();
        doorOpen = true;
        if (floor == limits.getTransferFloor()) {
            // Reach the transfer floor and open the door
            direction = limits.getZoneDirection();  // Hey, hey! Ensure the direction here
        }
        Iterator<PassageRequest> iterator = onboardRequests.iterator();
        while (iterator.hasNext()) {
            PassageRequest request = iterator.next();
            if (request.getToFloor() == floor) {
                // Arrive the destination
                FormattedPrinter.passengerLeave(request, floor, this);
                GlobalCounter.decrease();
                iterator.remove();
            } else if (floor == limits.getTransferFloor()) {
                // Reach the transfer floor
                if (!limits.reachable(request.getToFloor())) {
                    // Not this elevator's zone
                    FormattedPrinter.passengerLeave(request, floor, this);
                    request.setFromFloor(floor);
                    waitQueue.addRequest(request);
                    GlobalCounter.rawNotify();
                    iterator.remove();
                }
            }
        }
        return t;
    }

    public void closeDoor() {
        Iterator<PassageRequest> iterator = passageRequests.iterator();
        while (iterator.hasNext()) {
            PassageRequest request = iterator.next();
            if (request.getFromFloor() == floor
                    && onboardRequests.size() < limits.getMaxPassenger()
                    && request.sameDirection(direction)) {
                FormattedPrinter.passengerEnter(request, floor, this);
                onboardRequests.add(request);
                iterator.remove();
            }
        }
        doorOpen = false;
        FormattedPrinter.elevatorClose(this);
    }

    private ArrayList<PassageRequest> reset(int maxPassenger, double moveDurationSec) {
        ArrayList<PassageRequest> removed = new ArrayList<>(passageRequests);
        passageRequests.clear();
        if (!onboardRequests.isEmpty()) {
            if (!doorOpen) {
                // Unreachable!
                FormattedPrinter.elevatorOpen(this);
                doorOpen = true;
            }
            for (PassageRequest request : onboardRequests) {
                FormattedPrinter.passengerLeave(request, floor, this);
                if (request.getToFloor() != floor) {
                    request.setFromFloor(floor);  // Elevator will not move in reset
                    removed.add(request);
                } else {
                    GlobalCounter.decrease();
                }
            }
            onboardRequests.clear();
        }
        this.limits = new ElevatorLimits(maxPassenger, (long) (moveDurationSec * 1000));
        return removed;
    }

    public ArrayList<PassageRequest> reset(ResetRequest request) {
        return reset(request.getMaxPassenger(), request.getMoveDurationSec());
    }

    public long getMoveDurationMs() {
        return limits.getMoveDurationMs();
    }

    public int getMaxPassenger() {
        return limits.getMaxPassenger();
    }

    public ElevatorStatus getStatus() {
        // Synchronized here?
        // No! The status is generated by the elevator thread
        // And the elevator will not change its status during the generation
        return new ElevatorStatus(
                floor, doorOpen, direction, limits, passageRequests, onboardRequests
        );
    }

    public ElevatorStatus getStatus(long timeSnippet) {
        return new ElevatorStatus(
                timeSnippet, floor, doorOpen, direction, limits, passageRequests, onboardRequests
        );
    }

    public ElevatorLimits getLimits() {
        return limits;
    }

    public boolean reachable(PassageRequest request) {
        if (!limits.reachable(request.getFromFloor())) {
            return false;
        }
        // from floor is reachable
        return request.getFromFloor() != limits.getTransferFloor() ||
                limits.reachable(request.getToFloor());
    }
}
