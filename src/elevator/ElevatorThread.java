package elevator;

import controller.FormattedPrinter;
import controller.GlobalCounter;
import controller.Strategy;
import requests.BaseRequest;
import requests.PassageRequest;
import requests.RequestsQueue;
import requests.ResetRequest;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

public class ElevatorThread extends Thread {
    private final Elevator elevator;
    private final RequestsQueue<PassageRequest> requestsQueue;
    private final RequestsQueue<BaseRequest> waitQueue;
    private long timeSnippet;
    private final AtomicReference<ElevatorStatus> status;
    private final AtomicReference<ResetRequest> reset;
    private final Semaphore resetSemaphore;
    private final ElevatorThread buddy;
    private final AtomicBoolean readyToAccept;

    public ElevatorThread(
            int elevatorId, RequestsQueue<PassageRequest> requestsQueue,
            AtomicReference<ElevatorStatus> status, AtomicReference<ResetRequest> reset,
            Semaphore resetSemaphore, RequestsQueue<BaseRequest> waitQueue,
            ElevatorThread elevatorThread, ReentrantLock lock) {
        super(String.format("Thread-Elevator-%d-A", elevatorId));
        this.elevator = new Elevator(elevatorId, "" + elevatorId, lock);
        this.requestsQueue = requestsQueue;
        this.waitQueue = waitQueue;
        this.status = status;
        this.reset = reset;
        this.resetSemaphore = resetSemaphore;
        this.readyToAccept = new AtomicBoolean(true);
        this.buddy = elevatorThread;
        this.updateStatus();
        this.createTimeSnippet();
    }

    public ElevatorThread(
            int elevatorId, RequestsQueue<PassageRequest> requestsQueue,
            AtomicReference<ElevatorStatus> status, RequestsQueue<BaseRequest> waitQueue,
            ReentrantLock lock) {
        super(String.format("Thread-Elevator-%d-B", elevatorId));
        this.elevator = new Elevator(elevatorId, elevatorId + "-B", lock);
        this.requestsQueue = requestsQueue;
        this.waitQueue = waitQueue;
        this.status = status;
        this.reset = null;
        this.resetSemaphore = null;
        this.readyToAccept = new AtomicBoolean(false);
        this.buddy = null;
        this.updateStatus();
        this.createTimeSnippet();
    }

    private void updateStatus() {
        // Ensure the status is of the same elevator at the same time
        this.status.set(elevator.getStatus());
    }

    private void updateStatusWithTimeStamp() {
        this.status.set(elevator.getStatus(this.timeSnippet));
    }

    private void preciselySleep(long durationMS) {
        try {
            long t = Math.min(durationMS, timeSnippet + durationMS - System.currentTimeMillis());
            if (t <= 0) {
                return;
            }
            Thread.sleep(t);
        } catch (InterruptedException e) {
            // e.printStackTrace();
        }
    }

    private void createTimeSnippet() {
        this.timeSnippet = System.currentTimeMillis();
    }

    private void createTimeSnippet(long t) {
        this.timeSnippet = t;
    }

    private boolean tryGetRequest() {
        synchronized (this.requestsQueue) {
            if (this.requestsQueue.isEnd() && this.requestsQueue.isEmpty()
                    && elevator.canTerminate() && (reset == null || reset.get() == null)) {
                return false;
            }
            PassageRequest request = requestsQueue.popRequestWithoutWait();
            while (request != null) {
                // Add request to elevator directly
                if (elevator.reachable(request)) {
                    elevator.addRequest(request);
                    this.updateStatus();
                    FormattedPrinter.receiveRequest(request, elevator);
                } else {
                    waitQueue.addRequest(request);
                    this.updateStatus();
                    GlobalCounter.rawNotify();
                }
                try {
                    // How dare you! It's strange to wait for 1ms but useful?
                    this.requestsQueue.wait(1);
                } catch (InterruptedException e) {
                    // e.printStackTrace();
                }
                request = requestsQueue.popRequestWithoutWait();
            }
        }
        return true;
    }

    private void setBuddyAttribute(ResetRequest resetRequest, int transferFloor) {
        this.elevator.setOutputNameToA();
        buddy.elevator.reset(resetRequest);
        this.setElevatorFloor(1, transferFloor, transferFloor - 1);
        buddy.setElevatorFloor(transferFloor, 11, transferFloor + 1);
        this.updateStatus();
        buddy.updateStatus();
    }

    private void doReset() {
        try {
            resetSemaphore.acquire();
        } catch (InterruptedException e) {
            // e.printStackTrace();
        }
        boolean openedBefore = elevator.isDoorOpen();

        ResetRequest resetRequest = this.reset.get();
        ArrayList<PassageRequest> removed = elevator.reset(resetRequest);

        final int transferFloor = resetRequest.getTransferFloor();

        createTimeSnippet();
        if (elevator.isDoorOpen()) {
            if (!openedBefore) {
                preciselySleep(ElevatorLimits.OPENED_DURATION_MS);
            }
            elevator.closeDoor();  // The requestQueue will only be written by this thread
        }

        FormattedPrinter.resetBegin(elevator.getElevatorId());
        createTimeSnippet();
        this.updateStatusWithTimeStamp();

        // Clear the shared request queue between the scheduler and the elevator
        synchronized (this.requestsQueue) {
            PassageRequest request = this.requestsQueue.popRequestWithoutWait();
            while (request != null) {
                removed.add(request);
                request = this.requestsQueue.popRequestWithoutWait();
            }
            if (transferFloor > 0) {
                setBuddyAttribute(resetRequest, transferFloor);
                buddy.readyToAccept.set(true);
            }
        }

        synchronized (this.waitQueue) {
            for (PassageRequest request : removed) {
                request.setElevatorId(-1);
                this.waitQueue.addRequest(request);
            }
        }
        GlobalCounter.rawNotify();

        preciselySleep(ElevatorLimits.RESET_DURATION_MS);

        FormattedPrinter.resetEnd(elevator.getElevatorId());
        createTimeSnippet();
        buddy.createTimeSnippet();

        if (transferFloor > 0) {
            buddy.start();
        }

        synchronized (reset) {
            reset.set(null);  // Ensure that reset will not be written twice
            reset.notify();
        }
        resetSemaphore.release();
    }

    public void setElevatorFloor(int min, int max, int floor) {
        elevator.setLimitFloor(min, max, floor);
    }

    @Override
    public void run() {

        // The lock of requestsQueue will not occupy a lot of time, maybe
        while (tryGetRequest()) {
            if (reset != null && reset.get() != null) {
                this.doReset();
                continue;
            }
            this.updateStatus();  // Update status of the last loop
            // Maybe strange, and maybe not
            if (elevator.isDoorOpen()) {
                elevator.closeDoor();
                createTimeSnippet();
                continue;
            }
            // Handle the exist request or run the elevator
            Strategy.ElevatorStrategyType strategyType = Strategy.elevatorStrategy(elevator);
            // FormattedPrinter.debug(strategyType);
            if (strategyType == Strategy.ElevatorStrategyType.WAIT) {
                synchronized (this.requestsQueue) {
                    try {
                        this.requestsQueue.wait();  // Avoid polling timeout
                    } catch (InterruptedException e) {
                        // e.printStackTrace();
                    }
                }
            } else if (strategyType == Strategy.ElevatorStrategyType.MOVE) {
                elevator.ensureDirection();  // All tricks have been exhausted
                preciselySleep(elevator.getMoveDurationMs());
                tryGetRequest();
                strategyType = Strategy.elevatorStrategy(elevator);
                if (strategyType == Strategy.ElevatorStrategyType.OPEN) {
                    continue;
                }
                elevator.move();  // Output first
                createTimeSnippet();  // Then record the time
            } else if (strategyType == Strategy.ElevatorStrategyType.OPEN) {
                elevator.ensureDirection();
                createTimeSnippet(elevator.openDoor(waitQueue));
                preciselySleep(ElevatorLimits.OPENED_DURATION_MS);
                createTimeSnippet();
            } else if (strategyType == Strategy.ElevatorStrategyType.REVISE_MOVE) {
                preciselySleep(elevator.getMoveDurationMs());
                tryGetRequest();
                strategyType = Strategy.elevatorStrategy(elevator);
                if (strategyType == Strategy.ElevatorStrategyType.OPEN) {
                    continue;
                }
                elevator.moveReversely();  // Will ensure the direction
                createTimeSnippet();
            } else if (strategyType == Strategy.ElevatorStrategyType.REVISE_OPEN) {
                elevator.reverse();
                elevator.ensureDirection();
                createTimeSnippet(elevator.openDoor(waitQueue));
                preciselySleep(ElevatorLimits.OPENED_DURATION_MS);
            }
        }
        // System.out.printf("ElevatorThread-%d ends%n", elevator.getElevatorId());
    }

    public boolean isReadyToAccept() {
        return readyToAccept.get();
    }
}
