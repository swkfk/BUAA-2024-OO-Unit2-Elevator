package controller;

import elevator.Elevator;
import elevator.ElevatorDirection;
import elevator.ElevatorLimits;
import requests.PassageRequest;

import java.util.ArrayList;

public class Strategy {
    public enum ElevatorStrategyType {
        MOVE, OPEN, WAIT, REVISE_MOVE, REVISE_OPEN
    }

    public static ElevatorStrategyType elevatorStrategy(Elevator elevator) {
        ArrayList<PassageRequest> requests = elevator.getRequests();
        ArrayList<PassageRequest> onboards = elevator.getOnboards();
        ElevatorDirection direction = elevator.getDirection();
        int floor = elevator.getFloor();
        int maxPassenger = elevator.getMaxPassenger();
        ElevatorLimits limits = elevator.getLimits();
        // If reach the transfer floor, must open or move immediately
        if (limits.getTransferFloor() == floor) {
            // Have passengers wanting to leave at this floor or go to the floor unreachable
            if (hasTargetAhead(limits.getZoneDirection(), onboards, floor) ||
                    // Have requests under the reachable zone
                    hasRequestSameDirectionThisFloor(requests, floor, limits.getZoneDirection())) {
                if (direction == limits.getZoneDirection()) {
                    return ElevatorStrategyType.OPEN;
                } else {
                    return ElevatorStrategyType.REVISE_OPEN;
                }
            }
            // Leave the transfer floor immediately
            if (direction == limits.getZoneDirection()) {
                return ElevatorStrategyType.MOVE;
            } else {
                return ElevatorStrategyType.REVISE_MOVE;
            }
        }
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
            if (hasRemainedOnboards(onboards, floor)) {
                // Have passengers onboard
                return ElevatorStrategyType.OPEN;
            }
            // No requests ahead and No passengers at the same direction
            return ElevatorStrategyType.REVISE_OPEN;
        }
        // vvv No passengers want to leave vvv
        if (onboards.size() == maxPassenger) {
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

    private static boolean hasRemainedOnboards(ArrayList<PassageRequest> onboards, int floor) {
        for (PassageRequest onboard : onboards) {
            if (onboard.getToFloor() != floor) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasTargetAhead(
            ElevatorDirection zoneDirection, ArrayList<PassageRequest> onboards, int floor) {
        for (PassageRequest request : onboards) {
            if (request.getToFloor() == floor) {
                return true;
            }
            if (!zoneDirection.same(floor, request.getToFloor())) {
                // floor != request.getToFloor()
                // if floor > request.getToFloor() => Passenger go down, revise open!
                return true;
            }
        }
        return false;
    }
}
