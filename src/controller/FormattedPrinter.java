package controller;

import com.oocourse.elevator3.TimableOutput;
import elevator.Elevator;
import requests.PassageRequest;

public class FormattedPrinter {
    public static long passengerEnter(PassageRequest request, int floor, Elevator elevator) {
        return TimableOutput.println(
                String.format("IN-%d-%d-%s",
                        request.getPersonId(), floor, elevator.getOutputName()
                )
        );
    }

    public static long passengerLeave(PassageRequest request, int floor, Elevator elevator) {
        return TimableOutput.println(
                String.format("OUT-%d-%d-%s",
                        request.getPersonId(), floor, elevator.getOutputName()
                )
        );
    }

    public static long elevatorOpen(Elevator elevator) {
        return TimableOutput.println(
                String.format("OPEN-%d-%s", elevator.getFloor(), elevator.getOutputName())
        );
    }

    public static long elevatorClose(Elevator elevator) {
        return TimableOutput.println(
                String.format("CLOSE-%d-%s", elevator.getFloor(), elevator.getOutputName())
        );
    }

    public static long elevatorArrive(Elevator elevator) {
        return TimableOutput.println(
                String.format("ARRIVE-%d-%s", elevator.getFloor(), elevator.getOutputName())
        );
    }

    public static long receiveRequest(PassageRequest request, Elevator elevator) {
        return TimableOutput.println(
                String.format("RECEIVE-%d-%s", request.getPersonId(), elevator.getOutputName())
        );
    }

    public static long resetBegin(int elevatorId) {
        return TimableOutput.println(
                String.format("RESET_BEGIN-%d", elevatorId)
        );
    }

    public static long resetEnd(int elevatorId) {
        return TimableOutput.println(
                String.format("RESET_END-%d", elevatorId)
        );
    }

    public static long debug(Object obj) {
        return TimableOutput.println("[Debug]" + obj.toString());
    }

}
