import com.oocourse.elevator3.TimableOutput;
import controller.InputThread;
import controller.SchedulerThread;
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

public class Main {
    public static void main(String[] args) {
        TimableOutput.initStartTimestamp();

        RequestsQueue<BaseRequest> waitQueue = new RequestsQueue<>();
        ArrayList<RequestsQueue<PassageRequest>> processingQueues = new ArrayList<>();
        ArrayList<AtomicReference<ElevatorStatus>> elevatorStatuses = new ArrayList<>();
        ArrayList<AtomicReference<ResetRequest>> elevatorResets = new ArrayList<>();
        Semaphore resetSemaphore = new Semaphore(ElevatorLimits.ELEVATOR_COUNT);

        for (int i = 0; i < ElevatorLimits.ELEVATOR_COUNT; i++) {
            RequestsQueue<PassageRequest> parallelQueue = new RequestsQueue<>();
            processingQueues.add(parallelQueue);
            AtomicReference<ElevatorStatus> status = new AtomicReference<>();
            elevatorStatuses.add(status);
            AtomicReference<ResetRequest> reset = new AtomicReference<>();
            elevatorResets.add(reset);
            ElevatorThread elevator = new ElevatorThread(
                    i + 1, parallelQueue, status, reset, resetSemaphore, waitQueue);
            elevator.start();
        }

        SchedulerThread scheduler = new SchedulerThread(
                waitQueue, processingQueues, elevatorStatuses, elevatorResets, resetSemaphore);
        scheduler.start();

        InputThread inputThread = new InputThread(waitQueue);
        inputThread.start();

    }
}
