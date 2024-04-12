package controller;

import elevator.ElevatorLimits;
import elevator.ElevatorStatus;
import elevator.ElevatorThread;
import requests.BaseRequest;
import requests.PassageRequest;
import requests.RequestsQueue;
import requests.ResetRequest;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

public class SchedulerThread extends Thread {
    private final RequestsQueue<BaseRequest> waitQueue;
    private final ArrayList<RequestsQueue<PassageRequest>> passageRequestsQueues;  // len == 12
    private final ArrayList<AtomicReference<ElevatorStatus>> elevatorStatuses;  // len == 12
    private final ArrayList<AtomicReference<requests.ResetRequest>> elevatorResets;  // len == 6
    private final ArrayList<ElevatorThread> buddyThreads;  // len == 6
    private final ArrayList<ElevatorThread> threads;  // len == 6
    private final Semaphore resetSemaphore;

    public SchedulerThread(
            RequestsQueue<BaseRequest> waitQueue,
            ArrayList<RequestsQueue<PassageRequest>> passageRequestsQueues,
            ArrayList<AtomicReference<ElevatorStatus>> elevatorStatuses,
            ArrayList<AtomicReference<requests.ResetRequest>> elevatorResets,
            ArrayList<ElevatorThread> buddyThreads,
            ArrayList<ElevatorThread> mainThreads,
            Semaphore resetSemaphore
    ) {
        super("Thread-Scheduler");
        this.waitQueue = waitQueue;
        this.passageRequestsQueues = passageRequestsQueues;
        this.elevatorStatuses = elevatorStatuses;
        this.elevatorResets = elevatorResets;
        this.buddyThreads = buddyThreads;
        this.threads = new ArrayList<>(buddyThreads);
        this.threads.addAll(mainThreads);
        this.resetSemaphore = resetSemaphore;
    }

    @Override
    public void run() {
        while (true) {
            if (waitQueue.isEnd() && waitQueue.isEmpty() && passengerOver() && resetOver()) {
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
            try {
                resetSemaphore.acquire();
            } catch (InterruptedException e) {
                // e.printStackTrace();
            }
            if (request instanceof PassageRequest) {
                onPassageRequest((PassageRequest) request);
            } else if (request instanceof ResetRequest) {
                onResetRequest((ResetRequest) request);
            }
            resetSemaphore.release();
        }
        // System.out.println("SchedulerThread ends");
    }

    private void onPassageRequest(PassageRequest request) {
        // Do the scheduling
        // FormattedPrinter.passengerEnter(request);
        // System.out.println("Passenger " + request.getPersonId() + " To Schedule");
        request.setElevatorId(doPassengerSchedule(request));
        // FormattedPrinter.receiveRequest(request);
        int targetElevatorId = request.getElevatorId();
        passageRequestsQueues.get(targetElevatorId - 1).addRequest(request);
    }

    private int doPassengerSchedule(PassageRequest request) {
        /*
        for (AtomicReference<ElevatorStatus> status : elevatorStatuses) {
            System.out.println(status.get());
        }
        */
        long timeDelta;
        long minTimeDelta = Long.MAX_VALUE;
        int targetElevatorId = 0;
        int targetElevatorIdToChoose;
        for (int i = ElevatorLimits.ELEVATOR_COUNT; i < ElevatorLimits.ELEVATOR_COUNT * 2; ++i) {
            synchronized (passageRequestsQueues.get(i)) {
                synchronized (passageRequestsQueues.get(i - 6)) {
                    if (!buddyThreads.get(i - 6).isAlive()) {
                        // Not started yet
                        timeDelta = ShadowyCore.calculate(
                                elevatorStatuses.get(i).get(), request, passageRequestsQueues.get(i)
                        );
                        targetElevatorIdToChoose = i + 1;
                        // System.out.println("S: timeDelta: " + timeDelta + " targetElevatorIdToCh
                        // oose: " + targetElevatorIdToChoose);
                    } else {
                        // Have buddy thread
                        long[] ret = ShadowyCore.calculate(
                                elevatorStatuses.get(i).get(), passageRequestsQueues.get(i),
                                elevatorStatuses.get(i - 6).get(), passageRequestsQueues.get(i - 6),
                                request, i - 6
                        );
                        timeDelta = ret[0];
                        targetElevatorIdToChoose = (int) ret[1] + 1;
                        // System.out.println("D: timeDelta: " + timeDelta + " targetElevatorIdToCh
                        // oose: " + targetElevatorIdToChoose);
                    }
                }
            }
            // System.out.println("Elevator " + (i + 1) + " timeDelta: " + timeDelta);
            if (timeDelta < minTimeDelta) {
                minTimeDelta = timeDelta;
                targetElevatorId = targetElevatorIdToChoose;
            }
        }
        // System.out.println(request.getPersonId() + " To " + targetElevatorId);
        return targetElevatorId;
    }

    private void onResetRequest(ResetRequest request) {
        int targetElevatorId = request.getElevatorId();
        elevatorResets.get(targetElevatorId - 1).set(request);
        passageRequestsQueues.get(targetElevatorId - 1 + 6).rawNotify();
    }

    private boolean resetOver() {
        // If enter here, all inputted requests are processed
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

    private boolean passengerOver() {
        if (!GlobalCounter.zero()) {
            synchronized (GlobalCounter.class) {
                try {
                    GlobalCounter.class.wait();
                } catch (InterruptedException e) {
                    // e.printStackTrace();
                }
                return false;
            }
        }
        return true;
    }
}
