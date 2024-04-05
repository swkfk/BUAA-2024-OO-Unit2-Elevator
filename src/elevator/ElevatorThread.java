package elevator;

import controller.FormattedPrinter;
import controller.Strategy;
import requests.BaseRequest;
import requests.PassageRequest;
import requests.RequestsQueue;
import requests.ResetRequest;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

public class ElevatorThread extends Thread {
    private final Elevator elevator;
    private final RequestsQueue<PassageRequest> requestsQueue;
    private final RequestsQueue<BaseRequest> waitQueue;
    private long timeSnippet;
    private final AtomicReference<ElevatorStatus> status;
    private final AtomicReference<ResetRequest> reset;
    private final Semaphore resetSemaphore;

    public ElevatorThread(
            int elevatorId, RequestsQueue<PassageRequest> requestsQueue,
            AtomicReference<ElevatorStatus> status, AtomicReference<ResetRequest> reset,
            Semaphore resetSemaphore, RequestsQueue<BaseRequest> waitQueue) {
        super(String.format("Thread-Elevator-%d", elevatorId));
        this.elevator = new Elevator(elevatorId);
        this.requestsQueue = requestsQueue;
        this.waitQueue = waitQueue;
        this.status = status;
        this.reset = reset;
        this.resetSemaphore = resetSemaphore;
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
                    && elevator.canTerminate() && reset.get() == null) {
                return false;
            }
            PassageRequest request = requestsQueue.popRequestWithoutWait();
            while (request != null) {
                // Add request to elevator directly
                elevator.addRequest(request);
                FormattedPrinter.receiveRequest(request);
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

    private void doReset() {
        try {
            resetSemaphore.acquire();
        } catch (InterruptedException e) {
            // e.printStackTrace();
        }
        boolean openedBefore = elevator.isDoorOpen();
        ArrayList<PassageRequest> removed = elevator.reset(reset.get());
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

        synchronized (this.waitQueue) {
            for (PassageRequest request : removed) {
                request.setElevatorId(-1);
                this.waitQueue.addRequest(request);
            }
        }

        preciselySleep(ElevatorLimits.RESET_DURATION_MS);

        FormattedPrinter.resetEnd(elevator.getElevatorId());
        createTimeSnippet();
        this.updateStatus();

        synchronized (reset) {
            reset.set(null);  // Ensure that reset will not be written twice
            reset.notify();
        }
        resetSemaphore.release();
    }

    @Override
    public void run() {

        // The lock of requestsQueue will not occupy a lot of time, maybe
        while (tryGetRequest()) {
            if (reset.get() != null) {
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
            Strategy.ElevatorStrategyType strategyType = Strategy.elevatorStrategy(
                    elevator.getRequests(), elevator.getOnboards(),
                    elevator.getFloor(), elevator.getDirection(), elevator.getMaxPassenger()
            );
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
                preciselySleep(elevator.getMoveDurationMs());
                tryGetRequest();
                strategyType = Strategy.elevatorStrategy(
                        elevator.getRequests(), elevator.getOnboards(),
                        elevator.getFloor(), elevator.getDirection(), elevator.getMaxPassenger()
                );
                if (strategyType == Strategy.ElevatorStrategyType.OPEN) {
                    continue;
                }
                elevator.move();  // Output first
                createTimeSnippet();  // Then record the time
            } else if (strategyType == Strategy.ElevatorStrategyType.OPEN) {
                createTimeSnippet(elevator.openDoor());
                preciselySleep(ElevatorLimits.OPENED_DURATION_MS);
                createTimeSnippet();
            } else if (strategyType == Strategy.ElevatorStrategyType.REVISE_MOVE) {
                preciselySleep(elevator.getMoveDurationMs());
                tryGetRequest();
                strategyType = Strategy.elevatorStrategy(
                        elevator.getRequests(), elevator.getOnboards(),
                        elevator.getFloor(), elevator.getDirection(), elevator.getMaxPassenger()
                );
                if (strategyType == Strategy.ElevatorStrategyType.OPEN) {
                    continue;
                }
                elevator.moveReversely();
                createTimeSnippet();
            } else if (strategyType == Strategy.ElevatorStrategyType.REVISE_OPEN) {
                elevator.reverse();
                createTimeSnippet(elevator.openDoor());
                preciselySleep(ElevatorLimits.OPENED_DURATION_MS);
            }
        }
        // System.out.printf("ElevatorThread-%d ends%n", elevator.getElevatorId());
    }
}
