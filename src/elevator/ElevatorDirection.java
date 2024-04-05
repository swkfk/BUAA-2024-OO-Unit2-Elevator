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

    public boolean same(int from, int to) {
        return this == construct(from, to);
    }

    public ElevatorDirection reverse() {
        if (this == UP) {
            return DOWN;
        } else {
            return UP;
        }
    }
}
