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
        if (needOpenOut(onboards, floor) || needOpenIn(requests, onboards, floor, direction)) {
            return ElevatorStrategyType.OPEN;
        }
        // vvv No passengers want to leave vvv
        if (!onboards.isEmpty()) {
            // Have passengers onboard, move directly
            return ElevatorStrategyType.MOVE;
        }
        // vvv No people onboard vvv
        if (requests.isEmpty()) {
            // No requests in queue, wait
            return ElevatorStrategyType.WAIT;
        }
        // vvv Have requests somewhere vvv
        boolean requestSameDirection = false;
        // boolean requestReverseDirection = false;
        boolean requestGoReversely = false;
        for (PassageRequest request : requests) {
            ElevatorDirection due = ElevatorDirection.construct(floor, request.getFromFloor());
            if (request.getFromFloor() == floor) {
                // It must be reversed
                requestGoReversely = true;
            } else if (direction == due) {
                requestSameDirection = true;
            } /* else {
                requestReverseDirection = true;
            } */
        }
        if (requestSameDirection) {
            return ElevatorStrategyType.MOVE;
        } else if (requestGoReversely) {
            return ElevatorStrategyType.REVISE_OPEN;
        } else /*if (requestReverseDirection)*/ {
            return ElevatorStrategyType.REVISE_MOVE;
        }
    }

    private static boolean needOpenIn(
            ArrayList<PassageRequest> requests, ArrayList<PassageRequest> onboards,
            int floor, ElevatorDirection direction) {
        for (PassageRequest request : requests) {
            if (request.getFromFloor() == floor && request.sameDirection(direction)) {
                if (onboards.size() < ElevatorLimits.MAX_PASSENGER) {
                    return true;
                }
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
}
