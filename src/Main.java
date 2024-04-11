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

        ArrayList<ElevatorThread> elevatorBackupThreads = new ArrayList<>();
        for (int i = 0; i < ElevatorLimits.ELEVATOR_COUNT; i++) {
            RequestsQueue<PassageRequest> queue = new RequestsQueue<>();
            processingQueues.add(queue);
            AtomicReference<ElevatorStatus> status = new AtomicReference<>();
            elevatorStatuses.add(status);
            ElevatorThread elevatorBackup = new ElevatorThread(i + 1, queue, status, waitQueue);
            elevatorBackupThreads.add(elevatorBackup);
        }

        for (int i = 0; i < ElevatorLimits.ELEVATOR_COUNT; i++) {
            RequestsQueue<PassageRequest> queue = new RequestsQueue<>();
            processingQueues.add(queue);
            AtomicReference<ElevatorStatus> status = new AtomicReference<>();
            elevatorStatuses.add(status);
            AtomicReference<ResetRequest> reset = new AtomicReference<>();
            elevatorResets.add(reset);
            ElevatorThread elevatorMain = new ElevatorThread(
                    i + 1, queue, status, reset,
                    resetSemaphore, waitQueue, elevatorBackupThreads.get(i));
            elevatorMain.start();
        }

        SchedulerThread scheduler = new SchedulerThread(
                waitQueue, processingQueues, elevatorStatuses,
                elevatorResets, elevatorBackupThreads, resetSemaphore);
        scheduler.start();

        InputThread inputThread = new InputThread(waitQueue);
        inputThread.start();

    }
}
