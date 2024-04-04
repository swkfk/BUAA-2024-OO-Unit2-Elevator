package elevator;

import requests.PassageRequest;

import java.util.ArrayList;

public class ElevatorStatus {
    private final int floor;
    private final boolean opened;
    private final ElevatorDirection direction;
    private final ArrayList<PlainRequest> waitRequests;
    private final ArrayList<PlainRequest> onboardRequests;

    public ElevatorStatus(
            int floor, boolean opened, ElevatorDirection direction,
            ArrayList<PassageRequest> waitQueue, ArrayList<PassageRequest> onboardQueue) {
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
