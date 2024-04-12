package elevator;

import com.oocourse.elevator3.PersonRequest;
import requests.PassageRequest;

import java.util.ArrayList;
import java.util.LinkedList;

public class ElevatorStatus {
    private long resetStartTime;
    private final int floor;
    private final boolean opened;
    private final ElevatorDirection direction;
    private final ElevatorLimits limits;
    private final ArrayList<PlainRequest> waitRequests;
    private final ArrayList<PlainRequest> onboardRequests;

    public ElevatorStatus(
            int floor, boolean opened, ElevatorDirection direction, ElevatorLimits limits,
            ArrayList<PassageRequest> waitQueue, ArrayList<PassageRequest> onboardQueue) {
        this.resetStartTime = 0L;
        this.floor = floor;
        this.opened = opened;
        this.direction = direction;
        this.limits = limits;
        this.waitRequests = new ArrayList<>();
        for (PassageRequest request : waitQueue) {
            waitRequests.add(new PlainRequest(request));
        }
        this.onboardRequests = new ArrayList<>();
        for (PassageRequest request : onboardQueue) {
            onboardRequests.add(new PlainRequest(request));
        }
    }

    public ElevatorStatus(
            long timeSnippet, int floor, boolean opened,
            ElevatorDirection direction, ElevatorLimits limits,
            ArrayList<PassageRequest> waitQueue, ArrayList<PassageRequest> onboardQueue) {
        this(floor, opened, direction, limits, waitQueue, onboardQueue);
        this.resetStartTime = timeSnippet;
    }

    public ElevatorStatus(ElevatorStatus self) {
        this.resetStartTime = self.resetStartTime;
        this.floor = self.floor;
        this.opened = self.opened;
        this.direction = self.direction;
        this.limits = self.limits;
        // Shadow copy, but harmless
        this.waitRequests = new ArrayList<>(self.waitRequests);
        this.onboardRequests = new ArrayList<>(self.onboardRequests);
    }

    public long getResetStartTime() {
        return resetStartTime;
    }

    public int getFloor() {
        return floor;
    }

    public boolean isOpened() {
        return opened;
    }

    public ElevatorDirection getDirection() {
        return direction;
    }

    public ElevatorLimits getLimits() {
        return limits;
    }

    public ArrayList<PlainRequest> getWaitRequests() {
        return waitRequests;
    }

    public ArrayList<PlainRequest> getOnboardRequests() {
        return onboardRequests;
    }

    public ElevatorStatus withAdditionRequest(PassageRequest request) {
        ElevatorStatus newStatus = new ElevatorStatus(this);
        newStatus.waitRequests.add(new PlainRequest(request));
        return newStatus;
    }

    public ElevatorStatus withAdditionRequests(LinkedList<PassageRequest> requests) {
        ElevatorStatus newStatus = new ElevatorStatus(this);
        for (PassageRequest request : requests) {
            newStatus.waitRequests.add(new PlainRequest(request));
        }
        return newStatus;
    }

    public static class PlainRequest {
        private final int toFloor;
        private final int fromFloor;
        private final int passengerId;

        public PlainRequest(int toFloor, int fromFloor, int passengerId) {
            this.toFloor = toFloor;
            this.fromFloor = fromFloor;
            this.passengerId = passengerId;
        }

        public PlainRequest(PassageRequest request) {
            this(request.getToFloor(), request.getFromFloor(), request.getPersonId());
        }

        public int getToFloor() {
            return toFloor;
        }

        public int getFromFloor() {
            return fromFloor;
        }

        public boolean same(int otherId) {
            return this.passengerId == otherId;
        }

        @Override
        public String toString() {
            return "PlainRequest{" +
                    "toFloor=" + toFloor +
                    ", fromFloor=" + fromFloor +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "ElevatorStatus{" +
                "floor=" + floor +
                ", opened=" + opened +
                ", direction=" + direction +
                ", waitRequests=" + waitRequests +
                ", onboardRequests=" + onboardRequests +
                '}';
    }
}
