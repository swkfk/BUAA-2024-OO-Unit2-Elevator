package controller;

import elevator.ElevatorDirection;
import elevator.ElevatorLimits;
import requests.PassageRequest;

import java.util.ArrayList;

public class Strategy {
    public enum ElevatorStrategyType {
        MOVE, OPEN, WAIT, REVISE_MOVE, REVISE_OPEN
    }

    public static ElevatorStrategyType elevatorStrategy(
            ArrayList<PassageRequest> requests, ArrayList<PassageRequest> onboards,
            int floor, ElevatorDirection direction) {
        // If someone want to leave, must open the door!
        if (needOpenOut(onboards, floor)) {
            if (hasRequestSameDirectionThisFloor(requests, floor, direction)) {
                // Passenger with the same direction
                return ElevatorStrategyType.OPEN;
            }
            if (hasRequestsAhead(requests, floor, direction)) {
                // Have requests ahead
                return ElevatorStrategyType.OPEN;
            }
            // No requests ahead and No passengers at the same direction
            return ElevatorStrategyType.REVISE_OPEN;
        }
        // vvv No passengers want to leave vvv
        if (onboards.size() == ElevatorLimits.MAX_PASSENGER) {
            // Full of passengers, move directly
            return ElevatorStrategyType.MOVE;
        } else if (onboards.isEmpty()) {
            // No passengers onboard
            if (requests.isEmpty()) {
                // No requests in queue, wait
                return ElevatorStrategyType.WAIT;
            }
            // vvv Have requests somewhere vvv
            if (hasRequestSameDirectionThisFloor(requests, floor, direction)) {
                return ElevatorStrategyType.OPEN;
            }
            if (hasRequestsAhead(requests, floor, direction)) {
                return ElevatorStrategyType.MOVE;
            }
            // No requests ahead and No passengers at the same direction
            if (hasRequestDiffDirectionThisFloor(requests, floor, direction)) {
                return ElevatorStrategyType.REVISE_OPEN;
            }
            return ElevatorStrategyType.REVISE_MOVE;
        } else {
            // Have passengers onboard, but not full
            if (hasRequestSameDirectionThisFloor(requests, floor, direction)) {
                return ElevatorStrategyType.OPEN;
            }
            return ElevatorStrategyType.MOVE;
        }
    }

    private static boolean hasRequestSameDirectionThisFloor(
            ArrayList<PassageRequest> requests, int floor, ElevatorDirection direction) {
        for (PassageRequest request : requests) {
            if (request.getFromFloor() == floor && request.sameDirection(direction)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasRequestDiffDirectionThisFloor(
            ArrayList<PassageRequest> requests, int floor, ElevatorDirection direction) {
        for (PassageRequest request : requests) {
            if (request.getFromFloor() == floor && !request.sameDirection(direction)) {
                return true;
            }
        }
        return false;
    }

    private static boolean needOpenOut(ArrayList<PassageRequest> onboards, int floor) {
        for (PassageRequest onboard : onboards) {
            if (onboard.getToFloor() == floor) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasRequestsDown(ArrayList<PassageRequest> requests, int floor) {
        for (PassageRequest request : requests) {
            if (request.getFromFloor() < floor) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasRequestsUp(ArrayList<PassageRequest> requests, int floor) {
        for (PassageRequest request : requests) {
            if (request.getFromFloor() > floor) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasRequestsAhead(
            ArrayList<PassageRequest> requests, int floor, ElevatorDirection direction) {
        if (direction == ElevatorDirection.UP) {
            return hasRequestsUp(requests, floor);
        } else {
            return hasRequestsDown(requests, floor);
        }
    }
}
