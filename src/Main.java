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
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    public static void main(String[] args) {
        TimableOutput.initStartTimestamp();

        RequestsQueue<BaseRequest> waitQueue = new RequestsQueue<>();
        ArrayList<RequestsQueue<PassageRequest>> processingQueues = new ArrayList<>();
        ArrayList<AtomicReference<ElevatorStatus>> elevatorStatuses = new ArrayList<>();
        ArrayList<AtomicReference<ResetRequest>> elevatorResets = new ArrayList<>();
        Semaphore resetSemaphore = new Semaphore(ElevatorLimits.ELEVATOR_COUNT);
        ArrayList<ReentrantLock> transferLocks = new ArrayList<>();

        ArrayList<ElevatorThread> elevatorBackupThreads = new ArrayList<>();
        for (int i = 0; i < ElevatorLimits.ELEVATOR_COUNT; i++) {
            ReentrantLock lock = new ReentrantLock();
            transferLocks.add(lock);
            RequestsQueue<PassageRequest> queue = new RequestsQueue<>();
            processingQueues.add(queue);
            AtomicReference<ElevatorStatus> status = new AtomicReference<>();
            elevatorStatuses.add(status);
            ElevatorThread elevatorBackup = new ElevatorThread(
                    i + 1, queue, status, waitQueue, lock);
            elevatorBackupThreads.add(elevatorBackup);
        }

        ArrayList<ElevatorThread> elevatorMainThreads = new ArrayList<>();
        for (int i = 0; i < ElevatorLimits.ELEVATOR_COUNT; i++) {
            RequestsQueue<PassageRequest> queue = new RequestsQueue<>();
            processingQueues.add(queue);
            AtomicReference<ElevatorStatus> status = new AtomicReference<>();
            elevatorStatuses.add(status);
            AtomicReference<ResetRequest> reset = new AtomicReference<>();
            elevatorResets.add(reset);
            ElevatorThread elevatorMain = new ElevatorThread(
                    i + 1, queue, status, reset,
                    resetSemaphore, waitQueue, elevatorBackupThreads.get(i), transferLocks.get(i));
            elevatorMainThreads.add(elevatorMain);
            elevatorMain.start();
        }

        SchedulerThread scheduler = new SchedulerThread(
                waitQueue, processingQueues, elevatorStatuses,
                elevatorResets, elevatorBackupThreads, elevatorMainThreads, resetSemaphore);
        scheduler.start();

        InputThread inputThread = new InputThread(waitQueue);
        inputThread.start();

    }
}
