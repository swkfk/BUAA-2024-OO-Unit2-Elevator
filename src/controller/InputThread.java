package controller;

import com.oocourse.elevator2.ElevatorInput;
import com.oocourse.elevator2.PersonRequest;
import com.oocourse.elevator2.Request;
import com.oocourse.elevator2.ResetRequest;
import requests.BaseRequest;
import requests.PassageRequest;
import requests.RequestsQueue;

public class InputThread extends Thread {
    private final ElevatorInput elevatorInput;
    private final RequestsQueue<BaseRequest> waitQueue;

    public InputThread(RequestsQueue<BaseRequest> waitQueue) {
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
                    } else if (request instanceof ResetRequest) {
                        waitQueue.addRequest(new requests.ResetRequest((ResetRequest) request));
                    }
                }
            }
            elevatorInput.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
