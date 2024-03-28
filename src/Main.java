import com.oocourse.elevator1.TimableOutput;
import controller.InputThread;
import controller.SchedulerThread;
import elevator.ElevatorLimits;
import elevator.ElevatorThread;
import requests.PassageRequestsQueue;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        TimableOutput.initStartTimestamp();

        PassageRequestsQueue waitQueue = new PassageRequestsQueue();
        ArrayList<PassageRequestsQueue> processingQueues = new ArrayList<>();

        for (int i = 0; i < ElevatorLimits.ELEVATOR_COUNT; i++) {
            PassageRequestsQueue parallelQueue = new PassageRequestsQueue();
            processingQueues.add(parallelQueue);
            ElevatorThread elevator = new ElevatorThread(i + 1, parallelQueue);
            processingQueues.add(parallelQueue);
            elevator.start();
        }

        SchedulerThread scheduler = new SchedulerThread(waitQueue, processingQueues);
        scheduler.start();

        InputThread inputThread = new InputThread(waitQueue);
        inputThread.start();

    }
}
