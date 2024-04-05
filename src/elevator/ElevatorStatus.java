package elevator;

import requests.PassageRequest;

import java.util.ArrayList;

public class ElevatorStatus {
    private long resetStartTime;
    private final int floor;
    private final boolean opened;
    private final ElevatorDirection direction;
    private final ArrayList<PlainRequest> waitRequests;
    private final ArrayList<PlainRequest> onboardRequests;

    public ElevatorStatus(
            int floor, boolean opened, ElevatorDirection direction,
            ArrayList<PassageRequest> waitQueue, ArrayList<PassageRequest> onboardQueue) {
        this.resetStartTime = 0L;
        this.floor = floor;
        this.opened = opened;
        this.direction = direction;
        this.waitRequests = new ArrayList<>();
        for (PassageRequest request : waitQueue) {
            waitRequests.add(new PlainRequest(request.getToFloor(), request.getFromFloor()));
        }
        this.onboardRequests = new ArrayList<>();
        for (PassageRequest request : onboardQueue) {
            onboardRequests.add(new PlainRequest(request.getToFloor(), request.getFromFloor()));
        }
    }

    public ElevatorStatus(
            long timeSnippet, int floor, boolean opened, ElevatorDirection direction,
            ArrayList<PassageRequest> waitQueue, ArrayList<PassageRequest> onboardQueue) {
        this(floor, opened, direction, waitQueue, onboardQueue);
        this.resetStartTime = timeSnippet;
    }

    public ElevatorStatus(ElevatorStatus self) {
        this.resetStartTime = self.resetStartTime;
        this.floor = self.floor;
        this.opened = self.opened;
        this.direction = self.direction;
        // Shadow copy, but harmless
        this.waitRequests = new ArrayList<>(self.waitRequests);
        this.onboardRequests = new ArrayList<>(self.onboardRequests);
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

    public ArrayList<PlainRequest> getWaitRequests() {
        return waitRequests;
    }

    public ArrayList<PlainRequest> getOnboardRequests() {
        return onboardRequests;
    }

    public ElevatorStatus withAdditionRequest(PassageRequest request) {
        ElevatorStatus newStatus = new ElevatorStatus(this);
        newStatus.waitRequests.add(new PlainRequest(request.getToFloor(), request.getFromFloor()));
        return newStatus;
    }

    private static class PlainRequest {
        private final int toFloor;
        private final int fromFloor;

        public PlainRequest(int toFloor, int fromFloor) {
            this.toFloor = toFloor;
            this.fromFloor = fromFloor;
        }

        public int getToFloor() {
            return toFloor;
        }

        public int getFromFloor() {
            return fromFloor;
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
