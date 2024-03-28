package controller;

import com.oocourse.elevator1.PersonRequest;
import com.oocourse.elevator1.TimableOutput;
import elevator.Elevator;
import requests.PassageRequest;

public class FormattedPrinter {
    public static long passengerEnter(PassageRequest request) {
        return TimableOutput.println(
                String.format("IN-%d-%d-%d",
                        request.getPersonId(), request.getFromFloor(), request.getElevatorId()
                )
        );
    }

    public static long passengerLeave(PassageRequest request) {
        return TimableOutput.println(
                String.format("OUT-%d-%d-%d",
                        request.getPersonId(), request.getToFloor(), request.getElevatorId()
                )
        );
    }

    public static long elevatorOpen(Elevator elevator) {
        return TimableOutput.println(
                String.format("OPEN-%d-%d", elevator.getElevatorId(), elevator.getFloor())
        );
    }

    public static long elevatorClose(Elevator elevator) {
        return TimableOutput.println(
                String.format("CLOSE-%d-%d", elevator.getElevatorId(), elevator.getFloor())
        );
    }

    public static long elevatorArrive(Elevator elevator) {
        return TimableOutput.println(
                String.format("ARRIVE-%d-%d", elevator.getElevatorId(), elevator.getFloor())
        );
    }

    public static long debug(Elevator elevator) {
        return TimableOutput.println(
                String.format("DEBUG-Elevator-%d@%d", elevator.getElevatorId(), elevator.getFloor())
        );
    }

    public static long debug(PassageRequest request) {
        return TimableOutput.println(
                String.format("DEBUG-Person-%d@%d", request.getPersonId(), request.getElevatorId())
        );
    }
}
