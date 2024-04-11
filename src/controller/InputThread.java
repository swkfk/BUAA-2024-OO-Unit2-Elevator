package controller;

import com.oocourse.elevator3.ElevatorInput;
import com.oocourse.elevator3.Request;
import com.oocourse.elevator3.PersonRequest;
import com.oocourse.elevator3.NormalResetRequest;
import com.oocourse.elevator3.DoubleCarResetRequest;
import requests.BaseRequest;
import requests.PassageRequest;
import requests.RequestsQueue;
import requests.ResetRequest;

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
                    } else if (request instanceof NormalResetRequest) {
                        waitQueue.addRequest(new ResetRequest((NormalResetRequest) request));
                    } else if (request instanceof DoubleCarResetRequest) {
                        waitQueue.addRequest(new ResetRequest((DoubleCarResetRequest) request));
                    }
                }
            }
            elevatorInput.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
