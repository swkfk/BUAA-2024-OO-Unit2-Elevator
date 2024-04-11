package controller;

import elevator.ElevatorDirection;
import elevator.ElevatorLimits;
import elevator.ElevatorStatus;
import requests.PassageRequest;
import requests.RequestsQueue;

import java.util.ArrayList;
import java.util.Iterator;

public class ShadowyCore {
    public static long calculate(
            ElevatorStatus statusBase, RequestsQueue<PassageRequest> waitQueueBase,
            ElevatorStatus statusHigh, RequestsQueue<PassageRequest> waitQueueHigh,
            PassageRequest request) {
        return 1L;
    }

    public static long calculate(ElevatorStatus status, PassageRequest request,
                                 RequestsQueue<PassageRequest> waitQueue) {
        ElevatorStatus newStatus = status.withAdditionRequests(waitQueue.dangerousGetRequests());
        // System.out.println(newStatus);
        // long timeWithoutRequest = calculate(newStatus);
        return calculate(newStatus.withAdditionRequest(request));
    }

    private static long calculate(ElevatorStatus status) {
        // Basic Limits
        ElevatorLimits limits = status.getLimits();
        int maxPassenger = limits.getMaxPassenger();
        long moveDurationMs = limits.getMoveDurationMs();

        // Shallow Copied ArrayList
        ArrayList<ElevatorStatus.PlainRequest> waitRequests =
                new ArrayList<>(status.getWaitRequests());
        ArrayList<ElevatorStatus.PlainRequest> onboardRequests =
                new ArrayList<>(status.getOnboardRequests());

        long globalTime = -1;
        if (status.getResetStartTime() != 0) {
            globalTime = ElevatorLimits.RESET_DURATION_MS + status.getResetStartTime() -
                    System.currentTimeMillis();
        }
        globalTime = Math.max(globalTime, 0L);

        int floor = status.getFloor();
        ElevatorDirection direction = status.getDirection();

        if (status.isOpened()) {
            globalTime += ElevatorLimits.CLOSE_DURATION_MS;  // Not so accurate
            leaveElevator(floor, onboardRequests);
            enterSameDirection(direction, floor, maxPassenger, waitRequests, onboardRequests);
        }

        while (onboardRequests.size() != 0 || waitRequests.size() != 0) {
            if (hasPassengerOut(floor, onboardRequests)) {
                globalTime += ElevatorLimits.OPENED_DURATION_MS;
                leaveElevator(floor, onboardRequests);
                if (onboardRequests.size() == 0 &&
                        !hasRequestAhead(direction, floor, waitRequests) &&
                        !hasSameDirectionRequest(direction, floor, waitRequests)) {
                    direction = direction.reverse();
                }
                enterSameDirection(direction, floor, maxPassenger, waitRequests, onboardRequests);
            } else if (hasSameDirectionRequest(direction, floor, waitRequests) &&
                    hasSpace(onboardRequests, maxPassenger)) {
                globalTime += ElevatorLimits.OPENED_DURATION_MS;
                enterSameDirection(direction, floor, maxPassenger, waitRequests, onboardRequests);
            } else if (!hasRequestAhead(direction, floor, waitRequests) &&
                    hasOppositeDirectionRequest(direction, floor, waitRequests) &&
                    hasSpace(onboardRequests, maxPassenger)) {
                // No request ahead, but there is an opposite direction request
                // Change the direction
                direction = direction.reverse();
                globalTime += ElevatorLimits.OPENED_DURATION_MS;
                enterSameDirection(direction, floor, maxPassenger, waitRequests, onboardRequests);
            } else {
                if (floor == limits.getMinFloor()) {
                    direction = ElevatorDirection.UP;
                } else if (floor == limits.getMaxFloor()) {
                    direction = ElevatorDirection.DOWN;
                }
                globalTime += moveDurationMs;
                floor = move(direction, floor);
            }
        }

        return globalTime;
    }

    private static void leaveElevator(
            int floor,
            ArrayList<ElevatorStatus.PlainRequest> onboardRequests) {
        onboardRequests.removeIf(request -> request.getToFloor() == floor);
    }

    private static void enterSameDirection(
            ElevatorDirection direction, int floor, int max,
            ArrayList<ElevatorStatus.PlainRequest> waitRequests,
            ArrayList<ElevatorStatus.PlainRequest> onboardRequests) {
        Iterator<ElevatorStatus.PlainRequest> iterator = waitRequests.iterator();
        while (iterator.hasNext()) {
            ElevatorStatus.PlainRequest request = iterator.next();
            if (request.getFromFloor() == floor && direction.same(floor, request.getToFloor())) {
                onboardRequests.add(request);
                iterator.remove();
                if (onboardRequests.size() == max) {
                    break;
                }
            }
        }
    }

    private static int move(ElevatorDirection direction, int floor) {
        if (direction == ElevatorDirection.UP) {
            return floor + 1;
        } else {
            return floor - 1;
        }
    }

    private static boolean hasSameDirectionRequest(
            ElevatorDirection direction, int floor,
            ArrayList<ElevatorStatus.PlainRequest> waitRequests) {
        for (ElevatorStatus.PlainRequest request : waitRequests) {
            if (request.getFromFloor() == floor && direction.same(floor, request.getToFloor())) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasOppositeDirectionRequest(
            ElevatorDirection direction, int floor,
            ArrayList<ElevatorStatus.PlainRequest> waitRequests) {
        for (ElevatorStatus.PlainRequest request : waitRequests) {
            if (request.getFromFloor() == floor && !direction.same(floor, request.getToFloor())) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasPassengerOut(
            int floor, ArrayList<ElevatorStatus.PlainRequest> onboardRequests) {
        for (ElevatorStatus.PlainRequest request : onboardRequests) {
            if (request.getToFloor() == floor) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasRequestAhead(
            ElevatorDirection direction, int floor,
            ArrayList<ElevatorStatus.PlainRequest> waitRequests) {
        for (ElevatorStatus.PlainRequest request : waitRequests) {
            if (direction == ElevatorDirection.UP && request.getFromFloor() > floor) {
                return true;
            }
            if (direction == ElevatorDirection.DOWN && request.getFromFloor() < floor) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasSpace(
            ArrayList<ElevatorStatus.PlainRequest> onboardRequests, int max) {
        return onboardRequests.size() < max;
    }
}
