package elevator;

public enum ElevatorDirection {
    UP, DOWN;
    public static ElevatorDirection construct(int from, int to) {
        if (from < to) {
            return UP;
        } else {
            return DOWN;
        }
    }
}
