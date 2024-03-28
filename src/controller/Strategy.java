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
        if (needOpen(requests, onboards, floor, direction)) {
            return ElevatorStrategyType.OPEN;
        }
        // No need to open the door
        if (!onboards.isEmpty()) {
            return ElevatorStrategyType.MOVE;
        }
        // No people onboard
        if (requests.isEmpty()) {
            return ElevatorStrategyType.WAIT;
        }
        // Have requests somewhere
        int sameDirectionCount = 0;
        for (PassageRequest request : requests) {
            if (request.getFromFloor() == floor) {
                if (request.sameDirection(direction)) {
                    sameDirectionCount++;
                } else {
                    sameDirectionCount--;
                }
            }
            if (sameDirectionCount > 0) {
                return ElevatorStrategyType.OPEN;
            } else if (sameDirectionCount < 0) {
                return ElevatorStrategyType.REVISE_OPEN;
            }
        }
        // The elevator should move to the get the nearest request
        int upCost = 0;
        int downCost = 0;
        ElevatorDirection dueDirection;
        for (PassageRequest request : requests) {
            if (request.getFromFloor() > floor) {
                upCost += request.getFromFloor() - floor;
            } else {
                downCost += floor - request.getFromFloor();
            }
        }
        // TODO: We need a fine-tuned strategy here
        if (upCost < downCost) {
            dueDirection = ElevatorDirection.DOWN;
        } else {
            dueDirection = ElevatorDirection.UP;
        }
        // Move or move reversely
        if (dueDirection == direction) {
            return ElevatorStrategyType.MOVE;
        } else {
            return ElevatorStrategyType.REVISE_MOVE;
        }
    }

    private static boolean needOpen(
            ArrayList<PassageRequest> requests, ArrayList<PassageRequest> onboards,
            int floor, ElevatorDirection direction) {
        if (onboards.size() < ElevatorLimits.MAX_PASSENGER) {
            for (PassageRequest request : requests) {
                if (request.getFromFloor() == floor && request.sameDirection(direction)) {
                    return true;
                }
            }
        }
        for (PassageRequest onboard : onboards) {
            if (onboard.getToFloor() == floor) {
                return true;
            }
        }
        return false;
    }
}
