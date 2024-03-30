package elevator;

import controller.Strategy;
import requests.PassageRequest;
import requests.PassageRequestsQueue;

public class ElevatorThread extends Thread {
    private final Elevator elevator;
    private final PassageRequestsQueue requestsQueue;
    private long timeSnippet;

    public ElevatorThread(int elevatorId, PassageRequestsQueue requestsQueue) {
        super(String.format("Thread-Elevator-%d", elevatorId));
        this.elevator = new Elevator(elevatorId);
        this.requestsQueue = requestsQueue;
        this.createTimeSnippet();
    }

    private void preciselySleep(long durationMS) {
        try {
            Thread.sleep(
                    Math.min(durationMS,  timeSnippet + durationMS - System.currentTimeMillis())
            );
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

    @Override
    public void run() {

        // The lock of requestsQueue will not occupy a lot of time, maybe
        while (true) {
            synchronized (this.requestsQueue) {
                if (this.requestsQueue.isEnd() && this.requestsQueue.isEmpty()
                        && elevator.canTerminate()) {
                    break;
                }
                PassageRequest request = requestsQueue.popRequestWithoutWait();
                while (request != null) {
                    // Add request to elevator directly
                    elevator.addRequest(request);
                    try {
                        // How dare you! It's strange to wait for 1ms but useful?
                        this.requestsQueue.wait(1);
                    } catch (InterruptedException e) {
                        // e.printStackTrace();
                    }
                    request = requestsQueue.popRequestWithoutWait();
                }
            }
            // Maybe strange, and maybe not
            if (elevator.isDoorOpen()) {
                elevator.closeDoor();
                createTimeSnippet();
                continue;
            }
            // Handle the exist request or run the elevator
            Strategy.ElevatorStrategyType strategyType = Strategy.elevatorStrategy(
                    elevator.getRequests(), elevator.getOnboards(),
                    elevator.getFloor(), elevator.getDirection()
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
                preciselySleep(ElevatorLimits.MOVE_DURATION_MS);
                elevator.move();  // Output first
                createTimeSnippet();  // Then record the time
            } else if (strategyType == Strategy.ElevatorStrategyType.OPEN) {
                createTimeSnippet(elevator.openDoor());
                preciselySleep(ElevatorLimits.OPENED_DURATION_MS);
                createTimeSnippet();
            } else if (strategyType == Strategy.ElevatorStrategyType.REVISE_MOVE) {
                preciselySleep(ElevatorLimits.MOVE_DURATION_MS);
                elevator.moveReversely();
                createTimeSnippet();
            } else if (strategyType == Strategy.ElevatorStrategyType.REVISE_OPEN) {
                elevator.reverse();
                createTimeSnippet(elevator.openDoor());
                preciselySleep(ElevatorLimits.OPENED_DURATION_MS);
            }
        }
    }
}
