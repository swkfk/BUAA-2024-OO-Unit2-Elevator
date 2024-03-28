package elevator;

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

    @Override
    public void run() {
        while (true) {
            if (this.requestsQueue.isEnd() && this.requestsQueue.isEmpty()) {
                break;
            }
            PassageRequest request = requestsQueue.popRequestWithoutWait();
            if (request != null) {
                //
            }
        }
    }
}
