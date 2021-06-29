package byow.Core;

import java.util.ArrayList;
import java.util.List;

public class Position {
    private int x;
    private int y;

    public Position(int a, int b) {
        x = a;
        y = b;
    }

    /** Shifts this position over */
    public void shift(int dx, int dy) {
        x += dx;
        y += dy;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    /** Returns a new position based on the shift.
     * Does not mutate this object. */
    public Position newPositionShift(int dx, int dy) {
        return new Position(x + dx, y + dy);
    }

    public List<Position> surroundingPositions() {
        List<Position> surrounds = new ArrayList<Position>();
        for (int i = -1; i < 2; i++) {
            surrounds.add(new Position(x + i, y + 1)); // row above
            surrounds.add(new Position(x + i, y)); // middle row
            surrounds.add(new Position(x + i, y - 1)); // row below
        }
        surrounds.remove(new Position(x, y)); // remove position itself from surrounding
        return surrounds;
    }

}
