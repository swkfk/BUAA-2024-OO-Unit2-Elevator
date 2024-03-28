package elevator;

import controller.Strategy;
import requests.PassageRequest;
import requests.PassageRequestsQueue;

public class ElevatorThread extends Thread {
    private final Elevator elevator;
    private final PassageRequestsQueue requestsQueue;

    public ElevatorThread(int elevatorId, PassageRequestsQueue requestsQueue) {
        super(String.format("Thread-Elevator-%d", elevatorId));
        this.elevator = new Elevator(elevatorId);
        this.requestsQueue = requestsQueue;
    }

    private void preciselySleep(long durationMS) {
        try {
            // TODO: sleep precisely
            Thread.sleep(durationMS);
        } catch (InterruptedException e) {
            // e.printStackTrace();
        }
    }

    @Override
    public void run() {
        synchronized (this.requestsQueue) {
            // The lock of requestsQueue will not occupy a lot of time, maybe
            while (!this.requestsQueue.isEnd() || !this.requestsQueue.isEmpty()
                    || !elevator.canTerminate()) {
                PassageRequest request = requestsQueue.popRequestWithoutWait();
                while (request != null) {
                    // Add request to elevator directly
                    elevator.addRequest(request);
                    request = requestsQueue.popRequestWithoutWait();
                }
                // Maybe strange, and maybe not
                if (elevator.isDoorOpen()) {
                    elevator.closeDoor();
                    continue;
                }
                // Handle the exist request or run the elevator
                Strategy.ElevatorStrategyType strategyType = Strategy.elevatorStrategy(
                        elevator.getRequests(), elevator.getOnboards(),
                        elevator.getFloor(), elevator.getDirection()
                );
                if (strategyType == Strategy.ElevatorStrategyType.WAIT) {
                    try {
                        this.requestsQueue.wait();  // Avoid polling timeout
                    } catch (InterruptedException e) {
                        // e.printStackTrace();
                    }
                } else if (strategyType == Strategy.ElevatorStrategyType.MOVE) {
                    preciselySleep(ElevatorLimits.MOVE_DURATION_MS);
                    elevator.move();
                } else if (strategyType == Strategy.ElevatorStrategyType.OPEN) {
                    elevator.openDoor();
                    preciselySleep(ElevatorLimits.OPENED_DURATION_MS);
                } else if (strategyType == Strategy.ElevatorStrategyType.REVISE_MOVE) {
                    elevator.moveReversely();
                } else if (strategyType == Strategy.ElevatorStrategyType.REVISE_OPEN) {
                    elevator.reverse();
                    elevator.openDoor();
                    preciselySleep(ElevatorLimits.OPENED_DURATION_MS);
                }
            }
        }
    }
}
