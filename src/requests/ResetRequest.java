package requests;

public class ResetRequest extends BaseRequest {
    private final int elevatorId;
    private final int maxPassenger;
    private final double moveDurationSec;
    private final int transferFloor;

    // It shall be private, but the line limit is 100
    public ResetRequest(int elevatorId, int maxPassenger, double moveDurationS, int transferFloor) {
        this.elevatorId = elevatorId;
        this.maxPassenger = maxPassenger;
        this.moveDurationSec = moveDurationS;
        this.transferFloor = transferFloor;
    }

    public ResetRequest(com.oocourse.elevator3.NormalResetRequest resetRequest) {
        this(resetRequest.getElevatorId(), resetRequest.getCapacity(), resetRequest.getSpeed(), -1);
    }

    public ResetRequest(com.oocourse.elevator3.DoubleCarResetRequest resetRequest) {
        this(
                resetRequest.getElevatorId(), resetRequest.getCapacity(),
                resetRequest.getSpeed(), resetRequest.getTransferFloor()
        );
    }

    public int getElevatorId() {
        return elevatorId;
    }

    public int getMaxPassenger() {
        return maxPassenger;
    }

    public double getMoveDurationSec() {
        return moveDurationSec;
    }

    public int getTransferFloor() {
        return transferFloor;
    }
}
