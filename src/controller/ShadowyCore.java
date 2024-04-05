package controller;

import elevator.ElevatorStatus;
import requests.PassageRequest;

public class ShadowyCore {
    public static double calculate(ElevatorStatus status, PassageRequest request) {
        double timeWithoutRequest = calculate(status);
        double timeWithRequest = calculate(status.withAdditionRequest(request));
        return timeWithRequest - timeWithoutRequest;
    }

    private static double calculate(ElevatorStatus status) {
        return 0.0;
    }
}
