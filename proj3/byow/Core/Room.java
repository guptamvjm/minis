package byow.Core;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static byow.Core.Engine.max;
import static byow.Core.Engine.min;

public class Room {
    private int minX;
    private int minY;
    private int maxX;
    private int maxY;
    private List<Position> coords;
    private boolean isHall;
    private Position chest;
    private List<Position> coins;

    public Room(int miX, int miY, int maX, int maY, boolean hall) {
        minX = miX;
        minY = miY;
        maxX = maX;
        maxY = maY;
        isHall = hall;
        coords = new LinkedList<Position>();
        generateCoords();
    }

    public List<Position> coords() {
        return coords;
    }

    public void generateCoords() {
        for (int i = minX; i <= maxX; i++) {
            for (int j = minY; j <= maxY; j++) {
                coords.add(new Position(i, j));
            }
        }
    }

    public void setChest(Position p) {
        chest = p;
    }

    public void makeCoins(Random rand) {
        coins = new ArrayList<>();
        while (coins.size() < 10) {
            Position p = posInRoom(rand);
            coins.add(p);
        }
    }

    public Position posInRoom(Random rand) {
        int x = RandomUtils.uniform(rand, minX, maxX);
        int y = RandomUtils.uniform(rand, minY, maxY);
        return new Position(x, y);
    }

    public static Room genHall(Room r1, Room r2, Random rand) {
        //List<Position> hall = new LinkedList<Position>();
        Room higher = higherRoom(r1, r2);
        Room lower = r1;
        if (higher.equals(r1)) {
            lower = r2;
        }
        Room right = righterRoom(r1, r2);
        Room left = r1;
        if (right.equals(r1)) {
            left = r2;
        }

        boolean properRange = higher.maxY >= lower.maxY;
        if (right.minX >= left.minX && right.minX <= left.maxX && right.maxX >= left.maxX) {
            int hallX = RandomUtils.uniform(rand, right.minX, left.maxX + 1);
            return new Room(hallX, lower.maxY, hallX, higher.minY, true);
        } else if (higher.minY <= lower.maxY && higher.minY >= lower.minY && properRange) {
            int hallY = RandomUtils.uniform(rand, higher.minY, lower.maxY + 1);
            return new Room(left.maxX, hallY, right.minX, hallY, true);
        } else {
            Room yCoordSource = higher;
            if (right.equals(higher)) {
                yCoordSource = lower;
            }
            int x = RandomUtils.uniform(rand, right.minX, right.maxX + 1);
            int y = RandomUtils.uniform(rand, yCoordSource.minY, yCoordSource.maxY + 1);
            //Position intersection = new Position(x, y);

            Room verticalHall = new Room(x, min(y, right.maxY), x, max(y, right.maxY), true);
            Room horizontalHall = new Room(yCoordSource.minX, y, x, y, true);
            if (horizontalHall.coords.size() == 0 || verticalHall.coords.size() == 0) {
                return new Room(0, 0, -1, -1, false);
            }
            horizontalHall.coords.addAll(verticalHall.coords);
            return horizontalHall;
        }
    }

    public static Room higherRoom(Room r1, Room r2) {
        if (r1.maxY > r2.maxY) {
            return r1;
        } else {
            return r2;
        }
    }

    public static Room righterRoom(Room r1, Room r2) {
        if (r1.maxX > r2.maxX) {
            return r1;
        } else {
            return r2;
        }
    }

}
