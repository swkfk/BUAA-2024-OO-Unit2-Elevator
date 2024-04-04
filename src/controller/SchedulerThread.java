package controller;

import elevator.ElevatorStatus;
import requests.BaseRequest;
import requests.PassageRequest;
import requests.RequestsQueue;
import requests.ResetRequest;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class SchedulerThread extends Thread {
    private final RequestsQueue<BaseRequest> waitQueue;
    private final ArrayList<RequestsQueue<PassageRequest>> passageRequestsQueues;
    private final ArrayList<AtomicReference<ElevatorStatus>> elevatorStatuses;
    private final ArrayList<AtomicReference<requests.ResetRequest>> elevatorResets;
    private int passengerId = 0;

    public SchedulerThread(
            RequestsQueue<BaseRequest> waitQueue,
            ArrayList<RequestsQueue<PassageRequest>> passageRequestsQueues,
            ArrayList<AtomicReference<ElevatorStatus>> elevatorStatuses,
            ArrayList<AtomicReference<requests.ResetRequest>> elevatorResets
    ) {
        super("Thread-Scheduler");
        this.waitQueue = waitQueue;
        this.passageRequestsQueues = passageRequestsQueues;
        this.elevatorStatuses = elevatorStatuses;
        this.elevatorResets = elevatorResets;
    }

    @Override
    public void run() {
        while (true) {
            if (waitQueue.isEnd() && waitQueue.isEmpty() && resetOver()) {
                for (RequestsQueue<PassageRequest> passageRequestsQueue : passageRequestsQueues) {
                    passageRequestsQueue.setEnd();
                }
                break;
            }
            // Try to get a request from the waitQueue
            BaseRequest request = waitQueue.popRequest();
            if (request == null) {
                // waitQueue.wait();  // Useless because popRequest will wait
                continue;
            }
            if (request instanceof PassageRequest) {
                onPassageRequest((PassageRequest) request);
            } else if (request instanceof ResetRequest) {
                onResetRequest((ResetRequest) request);
            }
        }
        System.out.println("SchedulerThread ends");
    }

    private void onPassageRequest(PassageRequest request) {
        // Do the scheduling
        // FormattedPrinter.passengerEnter(request);
        request.setElevatorId(doPassengerSchedule(request));
        FormattedPrinter.receiveRequest(request);
        int targetElevatorId = request.getElevatorId();
        passageRequestsQueues.get(targetElevatorId - 1).addRequest(request);
    }

    private int doPassengerSchedule(PassageRequest request) {
        for (AtomicReference<ElevatorStatus> status : elevatorStatuses) {
            System.out.println(status.get());
        }
        passengerId++;
        return (passengerId % 6) + 1;
    }

    private void onResetRequest(ResetRequest request) {
        int targetElevatorId = request.getElevatorId();
        elevatorResets.get(targetElevatorId - 1).set(request);
        passageRequestsQueues.get(targetElevatorId - 1).rawNotify();
    }

    private boolean resetOver() {
        // If enter here, all inputted requests are processed
        System.out.println("resetOver");
        for (AtomicReference<ResetRequest> elevatorReset : elevatorResets) {
            synchronized (elevatorReset) {
                if (elevatorReset.get() != null) {
                    try {
                        elevatorReset.wait();
                    } catch (InterruptedException e) {
                        // e.printStackTrace();
                    }
                    return false;
                }
            }
        }
        return true;
    }
}
