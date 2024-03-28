package controller;

import com.oocourse.elevator1.ElevatorInput;
import com.oocourse.elevator1.PersonRequest;
import requests.PassageRequest;
import requests.PassageRequestsQueue;

public class InputThread extends Thread {
    private final ElevatorInput elevatorInput;
    private final PassageRequestsQueue waitQueue;

    public InputThread(PassageRequestsQueue waitQueue) {
        super("Thread-Input");
        this.elevatorInput = new ElevatorInput(System.in);
        this.waitQueue = waitQueue;
    }

    @Override
    public void run() {
        try {
            while (true) {
                PersonRequest request = elevatorInput.nextPersonRequest();
                if (request == null) {
                    waitQueue.setEnd();
                    break;
                } else {
                    waitQueue.addRequest(new PassageRequest(request));
                    // FormattedPrinter.debug(new PassageRequest(request));
                }
            }
            elevatorInput.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
