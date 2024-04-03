package controller;

import com.oocourse.elevator2.ElevatorInput;
import com.oocourse.elevator2.PersonRequest;
import com.oocourse.elevator2.Request;
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
                Request request = elevatorInput.nextRequest();
                if (request == null) {
                    waitQueue.setEnd();
                    break;
                } else {
                    if (request instanceof PersonRequest) {
                        waitQueue.addRequest(new PassageRequest((PersonRequest) request));
                    }
                }
            }
            elevatorInput.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
