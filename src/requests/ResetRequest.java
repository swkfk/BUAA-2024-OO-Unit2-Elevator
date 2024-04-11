package requests;

public class ResetRequest extends BaseRequest {
    private final int elevatorId;
    private final int maxPassenger;
    private final double moveDurationSec;

    public ResetRequest(int elevatorId, int maxPassenger, double moveDurationS) {
        this.elevatorId = elevatorId;
        this.maxPassenger = maxPassenger;
        this.moveDurationSec = moveDurationS;
    }

    public ResetRequest(com.oocourse.elevator3.NormalResetRequest resetRequest) {
        this(resetRequest.getElevatorId(), resetRequest.getCapacity(), resetRequest.getSpeed());
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
}
