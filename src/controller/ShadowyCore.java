package controller;

import elevator.ElevatorDirection;
import elevator.ElevatorLimits;
import elevator.ElevatorStatus;
import requests.PassageRequest;
import requests.RequestsQueue;

import java.util.ArrayList;
import java.util.Iterator;

public class ShadowyCore {
    private static final long ELECTRICITY_MOVE = 80L;
    private static final long ELECTRICITY_OPEN = 20L;

    public static long[] calculate(
            ElevatorStatus statusMain, RequestsQueue<PassageRequest> waitQueueMain,
            ElevatorStatus statusBuddy, RequestsQueue<PassageRequest> waitQueueBuddy,
            PassageRequest request, int upId) {
        int downId = upId + 6;
        int targetId = request.getPersonId();
        int transferFloor = statusMain.getLimits().getTransferFloor();
        if (request.getFromFloor() <= transferFloor &&
                request.getToFloor() <= transferFloor) {
            ElevatorStatus status = statusMain.withAdditionRequests(
                    waitQueueMain.dangerousGetRequests());
            long timeDelta = Math.max(
                    calculate(status.withAdditionRequest(request), 4L, -1)[0],
                    calculate(statusBuddy.withAdditionRequests(
                            waitQueueBuddy.dangerousGetRequests()), 4L, -1)[0]
            );
            return new long[]{timeDelta, downId};
        } else if (request.getFromFloor() >= transferFloor &&
                request.getToFloor() >= transferFloor) {
            ElevatorStatus status = statusBuddy.withAdditionRequests(
                    waitQueueBuddy.dangerousGetRequests());
            long timeDelta = Math.max(
                    calculate(status.withAdditionRequest(request), 4L, -1)[0],
                    calculate(statusMain.withAdditionRequests(waitQueueMain.dangerousGetRequests()),
                            4L, -1)[0]
            );
            return new long[]{timeDelta, upId};
        } else if (request.getFromFloor() < transferFloor) {
            ElevatorStatus status = statusMain.withAdditionRequests(
                    waitQueueMain.dangerousGetRequests());
            long[] res = calculate(status.withAdditionRequest(request), 4L, targetId);
            long timeDeltaDown = res[0];
            ElevatorStatus.PlainRequest requestP = new ElevatorStatus.PlainRequest(request);
            status = statusBuddy.withAdditionRequests(waitQueueBuddy.dangerousGetRequests());
            requestP.setFromFloor(transferFloor);
            long timeDeltaUp = calculate(status, 4L, targetId, res[1], requestP)[0];
            return new long[]{Math.max(timeDeltaDown, timeDeltaUp), downId};
        } else if (request.getFromFloor() > transferFloor) {
            ElevatorStatus status = statusBuddy
                    .withAdditionRequests(waitQueueBuddy.dangerousGetRequests())
                    .withAdditionRequest(request);
            long[] res = calculate(status, 4L, targetId);
            long timeDeltaUp = res[0];
            ElevatorStatus.PlainRequest requestP = new ElevatorStatus.PlainRequest(request);
            status = statusMain.withAdditionRequests(waitQueueMain.dangerousGetRequests());
            requestP.setFromFloor(transferFloor);
            long timeDeltaDown = calculate(status, 4L, targetId, res[1], requestP)[0];
            return new long[]{Math.max(timeDeltaDown, timeDeltaUp), upId};
        }
        // System.out.println("Up ID: " + upId);
        // System.out.println("Down ID: " + downId);
        // System.out.println("Transfer Floor: " + transferFloor);
        // System.out.println("From: " + request.getFromFloor());
        // System.out.println("To: " + request.getToFloor());
        // System.out.println("Target ID: " + targetId);
        return new long[]{10000001L, -1L};  // Unreachable
    }

    public static long calculate(ElevatorStatus status, PassageRequest request,
                                 RequestsQueue<PassageRequest> waitQueue) {
        ElevatorStatus newStatus = status.withAdditionRequests(waitQueue.dangerousGetRequests());
        // System.out.println(newStatus);
        // long timeWithoutRequest = calculate(newStatus);
        return calculate(newStatus.withAdditionRequest(request), 1L, -1)[0];
    }

    private static long[] calculate(ElevatorStatus status, long electricityRatio, int targetId) {
        return calculate(status, electricityRatio, targetId, -1L, null);
    }

    private static long[] calculate(ElevatorStatus status, long electricityRatio, int targetId,
                                    long arriveTime, ElevatorStatus.PlainRequest request) {
        InsertPack insertPack = new InsertPack(arriveTime, targetId);
        long electricity = 0L;
        // Basic Limits
        ElevatorLimits limits = status.getLimits();
        int maxPassenger = limits.getMaxPassenger();
        long moveDurationMs = limits.getMoveDurationMs();
        // if (limits.getTransferFloor() != -1) {
        //     // Abnormal Case
        //     return new long[]{10000002L, -1L};
        // }

        // Shallow Copied ArrayList
        ArrayList<ElevatorStatus.PlainRequest> waitRs = new ArrayList<>(status.getWaitRequests());
        ArrayList<ElevatorStatus.PlainRequest> onRs = new ArrayList<>(status.getOnboardRequests());

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
            if (leaveElevator(floor, insertPack.transferId, limits, onRs)) {
                insertPack.leave(globalTime);
            }
            enterSameDirection(direction, floor, maxPassenger, waitRs, onRs);
            electricity += ELECTRICITY_OPEN;
        }

        while ((onRs.size() != 0 || waitRs.size() != 0 || insertPack.insertTime >= 0)
                && globalTime < 400000) {
            globalTime = insertPack.insertSkip(globalTime, request, waitRs, onRs);
            insertPack.insert(globalTime, request, waitRs);

            if (hasPassengerOut(floor, limits, onRs)) {
                globalTime += ElevatorLimits.OPENED_DURATION_MS;
                if (leaveElevator(floor, insertPack.transferId, limits, onRs)) {
                    insertPack.leave(globalTime);
                }
                if (onRs.size() == 0 && !hasReqAhead(direction, floor, waitRs) &&
                        !hasSameDirReq(direction, floor, waitRs)) {
                    direction = direction.reverse();
                }
                // Ensure that the elevator's direction is correct
                direction = ensureDirection(direction, floor, limits);
                enterSameDirection(direction, floor, maxPassenger, waitRs, onRs);
                electricity += ELECTRICITY_OPEN * 2;
            } else if (hasSameDirReq(direction, floor, waitRs) && hasSpace(onRs, maxPassenger)) {
                globalTime += ElevatorLimits.OPENED_DURATION_MS;
                enterSameDirection(direction, floor, maxPassenger, waitRs, onRs);
                electricity += ELECTRICITY_OPEN * 2;
            } else if (!hasReqAhead(direction, floor, waitRs) &&
                    hasOppoDirReq(direction, floor, waitRs) && hasSpace(onRs, maxPassenger)) {
                // No request ahead, but there is an opposite direction request
                // Change the direction
                direction = direction.reverse();
                globalTime += ElevatorLimits.OPENED_DURATION_MS;
                enterSameDirection(direction, floor, maxPassenger, waitRs, onRs);
                electricity += ELECTRICITY_OPEN * 2;
            } else {
                direction = ensureDirection(direction, floor, limits);
                globalTime += moveDurationMs;
                floor = move(direction, floor);
                electricity += ELECTRICITY_MOVE;
            }
        }

        return new long[]{globalTime + electricity / electricityRatio, insertPack.leaveTime};
    }

    private static ElevatorDirection ensureDirection(
            ElevatorDirection direction, int floor, ElevatorLimits limits) {
        if (floor == limits.getMinFloor()) {
            return ElevatorDirection.UP;
        } else if (floor == limits.getMaxFloor()) {
            return ElevatorDirection.DOWN;
        }
        return direction;
    }

    private static boolean leaveElevator(
            int floor, int targetId, ElevatorLimits limits,
            ArrayList<ElevatorStatus.PlainRequest> onboardRequests) {
        boolean gotTarget = false;
        Iterator<ElevatorStatus.PlainRequest> iterator = onboardRequests.iterator();
        while (iterator.hasNext()) {
            ElevatorStatus.PlainRequest request = iterator.next();
            if (request.getToFloor() == floor) {
                iterator.remove();
            } else if (floor == limits.getTransferFloor() &&
                    !limits.reachable(request.getToFloor())) {
                iterator.remove();
            }
            if (request.same(targetId)) {
                gotTarget = true;
            }
        }
        return gotTarget;
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

    private static boolean hasSameDirReq(
            ElevatorDirection direction, int floor,
            ArrayList<ElevatorStatus.PlainRequest> waitRequests) {
        for (ElevatorStatus.PlainRequest request : waitRequests) {
            if (request.getFromFloor() == floor && direction.same(floor, request.getToFloor())) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasOppoDirReq(
            ElevatorDirection direction, int floor,
            ArrayList<ElevatorStatus.PlainRequest> waitRequests) {
        for (ElevatorStatus.PlainRequest request : waitRequests) {
            if (request.getFromFloor() == floor && !direction.same(floor, request.getToFloor())) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasPassengerOut(int floor, ElevatorLimits limits,
                                           ArrayList<ElevatorStatus.PlainRequest> onboardRequests) {
        for (ElevatorStatus.PlainRequest request : onboardRequests) {
            if (request.getToFloor() == floor) {
                return true;
            }
            if (floor == limits.getTransferFloor() && !limits.reachable(request.getToFloor())) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasReqAhead(
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

    private static class InsertPack {
        private long insertTime;
        private long leaveTime;
        private int transferId;

        public InsertPack(long insertTime, int transferId) {
            this.insertTime = insertTime;
            this.leaveTime = -1L;
            this.transferId = transferId;
        }

        public void leave(long leaveTime) {
            this.leaveTime = leaveTime;
            this.transferId = -1;
        }

        public void insert(long globalTime, ElevatorStatus.PlainRequest request,
                           ArrayList<ElevatorStatus.PlainRequest> waitRs) {
            if (globalTime >= insertTime && request != null && insertTime >= 0) {
                insertTime = -1;
                waitRs.add(request);
            }
        }

        public long insertSkip(
                long globalTime, ElevatorStatus.PlainRequest request,
                ArrayList<ElevatorStatus.PlainRequest> waitRs,
                ArrayList<ElevatorStatus.PlainRequest> onRs) {
            if (onRs.size() == 0 && waitRs.size() == 0 && request != null) {
                insertTime = -1;
                waitRs.add(request);
                return Math.max(globalTime, insertTime);
            }
            return globalTime;
        }
    }
}
