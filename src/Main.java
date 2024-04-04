import com.oocourse.elevator2.TimableOutput;
import controller.InputThread;
import controller.SchedulerThread;
import elevator.ElevatorLimits;
import elevator.ElevatorStatus;
import elevator.ElevatorThread;
import requests.PassageRequestsQueue;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class Main {
    public static void main(String[] args) {
        TimableOutput.initStartTimestamp();

        PassageRequestsQueue waitQueue = new PassageRequestsQueue();
        ArrayList<PassageRequestsQueue> processingQueues = new ArrayList<>();
        ArrayList<AtomicReference<ElevatorStatus>> elevatorStatuses = new ArrayList<>();

        for (int i = 0; i < ElevatorLimits.ELEVATOR_COUNT; i++) {
            PassageRequestsQueue parallelQueue = new PassageRequestsQueue();
            AtomicReference<ElevatorStatus> status = new AtomicReference<>();
            processingQueues.add(parallelQueue);
            elevatorStatuses.add(status);
            ElevatorThread elevator = new ElevatorThread(i + 1, parallelQueue, status);
            elevator.start();
        }

        SchedulerThread scheduler =
                new SchedulerThread(waitQueue, processingQueues, elevatorStatuses);
        scheduler.start();

        InputThread inputThread = new InputThread(waitQueue);
        inputThread.start();

    }
}
