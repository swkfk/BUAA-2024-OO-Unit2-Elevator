package controller;

import requests.PassageRequest;
import requests.PassageRequestsQueue;

import java.util.ArrayList;

public class SchedulerThread extends Thread {
    private final PassageRequestsQueue waitQueue;
    private final ArrayList<PassageRequestsQueue> passageRequestsQueues;
    private int passengerId = 0;

    public SchedulerThread(
            PassageRequestsQueue waitQueue, ArrayList<PassageRequestsQueue> passageRequestsQueues
    ) {
        super("Thread-Scheduler");
        this.waitQueue = waitQueue;
        this.passageRequestsQueues = passageRequestsQueues;
    }

    @Override
    public void run() {
        while (true) {
            if (waitQueue.isEnd() && waitQueue.isEmpty()) {
                for (PassageRequestsQueue passageRequestsQueue : passageRequestsQueues) {
                    passageRequestsQueue.setEnd();
                }
                break;
            }
            // Try get a request from the waitQueue
            PassageRequest request = waitQueue.popRequest();
            if (request == null) {
                // waitQueue.wait();  // Useless because popRequest will wait
                continue;
            }
            // Do the scheduling
            // FormattedPrinter.passengerEnter(request);
            request.setElevatorId(doSchedule(request));
            FormattedPrinter.receiveRequest(request);
            int targetElevatorId = request.getElevatorId();
            passageRequestsQueues.get(targetElevatorId - 1).addRequest(request);
        }
    }

    private int doSchedule(PassageRequest request) {
        passengerId++;
        return (passengerId % 6) + 1;
    }
}
