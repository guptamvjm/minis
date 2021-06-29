package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Engine {
    /* Feel free to change the width and height. */
    public static final int WIDTH = 50;
    public static final int HEIGHT = 50;
    public static final Font BIG_FONT = new Font("SansSerif", Font.BOLD, 35);
    public static final Font SMALL_FONT = new Font("Monaco", Font.BOLD, 14);
    public static final File CWD = new File(System.getProperty("user.dir"));

    /** File that holds the input string the represents the current state **/
    public static final File SAVEFILE = Utils.join(CWD, "save.txt");

    private TERenderer ter = new TERenderer();
    private Random numberGenerator;
    private String inputString = "";
    private Position avatarPos;
    private String avatarName = "";

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
        makeFile(SAVEFILE);
        setupDraw();
        mainMenu();
        char option = solicitOption();
        if (option == 'N') {
            Long seed = solicitSeed();
            inputString = "N" + seed + "S";
            StdDraw.clear(Color.BLACK);
            TETile[][] world = makeTheWorld(seed);
            moveWithKeyBoard(world);
        } else if (option == 'L') {
            TETile[][] world = loadWorld(true);
            if (world != null) {
                moveWithKeyBoard(world);
            }
        } else if (option == 'E') {
            avatarName = solicitName();
            interactWithKeyboard();
        } else if (option == 'R') {
            replay();
        } else if (option == 'Q') {
            System.exit(0);
        }


    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     *
     * In other words, both of these calls:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     *
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        // passed in as an argument, and return a 2D tile representation of the
        // world that would have been drawn if the same inputs had been given
        // to interactWithKeyboard().
        //
        // See proj3.byow.InputDemo for a demo of how you can make a nice clean interface
        // that works for many different input types.
        makeFile(SAVEFILE);
        setupDraw();
        if (input.charAt(0) == 'L' || input.charAt(0) == 'l') {
            TETile[][] world = loadWorld(false);
            if (world == null) {
                return null;
            }
            moveWithText(input.substring(1), world);
            ter.renderFrame(world);
            return world;
        } else {
            Long seed = parseInput(input);
            inputString = "N" + seed + "S";
            TETile[][] finalWorldFrame = makeTheWorld(seed);
            String moveString = input.substring(endSeedIndex(input) + 1);
            moveWithText(moveString, finalWorldFrame);
            ter.renderFrame(finalWorldFrame);
            return finalWorldFrame;
        }
    }

    /** Interacts with input string from SAVEFILE **/
    public TETile[][] loadInteractInputString(String input) {
        makeFile(SAVEFILE);
        String s = input.substring(1, endSeedIndex(input));
        Long seed = Long.parseLong(s);
        TETile[][] finalWorldFrame = makeTheWorld(seed);
        String moveString = input.substring(endSeedIndex(input) + 1);
        inputString = 'N' + s + 'S';
        moveWithText(moveString, finalWorldFrame);
        inputString = inputString.substring(0, inputString.length() - 1);
        return finalWorldFrame;
    }

    /** Method to intialize StdDraw **/
    public void setupDraw() {
        ter.initialize(WIDTH, HEIGHT + 5);
        StdDraw.setFont(BIG_FONT);
        StdDraw.setPenColor(Color.WHITE);
    }

    /** Method that shows the main menu **/
    public void mainMenu() {
        StdDraw.clear(Color.BLACK);
        StdDraw.text(WIDTH / 2, 2 * HEIGHT / 3, "BYOW");
        StdDraw.text(WIDTH / 2, HEIGHT / 3 + 2, "New Game (N)");
        StdDraw.text(WIDTH / 2, HEIGHT / 3, "Load Game (L)");
        StdDraw.text(WIDTH / 2, HEIGHT / 3 - 2, "Enter Name (E)");
        StdDraw.text(WIDTH / 2, HEIGHT / 3 - 4, "Replay Game (R)");
        StdDraw.text(WIDTH / 2, HEIGHT / 3 - 6, "Quit (Q)");
        StdDraw.show();
    }

    /** Generates a world from a seed */
    public TETile[][] makeTheWorld(Long seed) {
        TETile[][] finalWorldFrame = new TETile[WIDTH][HEIGHT];
        numberGenerator = new Random(seed);
        ter.initialize(WIDTH, HEIGHT + 2);
        fillWithNothing(finalWorldFrame);
        generateTwo(finalWorldFrame);
        makeWalls(finalWorldFrame);
        avatarPos = randomRoomPosition(finalWorldFrame);
        updateAvatar(finalWorldFrame);
        ter.renderFrame(finalWorldFrame);
        return finalWorldFrame;
    }

    /** X1 Initializes world with nothing in it **/
    public static void fillWithNothing(TETile[][] tiles) {
        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles[0].length; j++) {
                tiles[i][j] = Tileset.NOTHING;
            }
        }
    }

    /** Generates the world. Called generateTwo because it replaced another
     * world generation algorithm.
     */
    public void generateTwo(TETile[][] tiles) {
        List<Room> rooms = new ArrayList<>();
        int k = 12;
        for (int i = 0; i < k; i++) {
            int xMin = RandomUtils.uniform(numberGenerator, 1, WIDTH / 2 - 5);
            int xMax = RandomUtils.uniform(numberGenerator, xMin + 2, max(WIDTH / 2, xMin + 7));
            int yMin = RandomUtils.uniform(numberGenerator, 1, HEIGHT / 2 - 5);
            int yMax = RandomUtils.uniform(numberGenerator, yMin + 2, max(HEIGHT / 2, yMin + 7));
            if (i > k / 2) {
                yMin = RandomUtils.uniform(numberGenerator, HEIGHT / 2, HEIGHT - 5);
                yMax = RandomUtils.uniform(numberGenerator, yMin + 2, max(HEIGHT - 2, yMin + 7));
            }
            if (i > k / 4 && i < 3 * k / 4) {
                xMin = RandomUtils.uniform(numberGenerator, WIDTH / 2, WIDTH - 5);
                xMax = RandomUtils.uniform(numberGenerator, xMin + 2, max(WIDTH - 2, xMin + 7));
            }
            rooms.add(new Room(xMin, yMin, xMax, yMax, false));
        }
        for (int i = 0; i < rooms.size() - 1; i++) {
            Room h = Room.genHall(rooms.get(i), rooms.get(i + 1), numberGenerator);
            Room r = Room.genHall(rooms.get(i), rooms.get(i / 2), numberGenerator);
            change(tiles, h.coords(), Tileset.WATER);
            change(tiles, r.coords(), Tileset.WATER);
            change(tiles, rooms.get(i).coords(), Tileset.WATER);
        }

        /*
        Note: These lines would be used in implementing chests.
        Room x = getRandomRoom(rooms);
        Position p = x.posInRoom(numberGenerator);
        x.setChest(p);
        tiles[p.x][p.y] = Tileset.CHEST;
         */
    }

    /** NOT IN USE FOR OFFICIAL SUB. Method for when chests are implemented **/
    public Room getRandomRoom(List<Room> rooms) {
        int x = RandomUtils.uniform(numberGenerator, rooms.size());
        return rooms.get(x);
    }

    /** NOT IN USE FOR OFFICIAL SUB. Method for when chests are implemented **/
    public Room makeChestRoom(TETile[][] tiles) {
        int x = WIDTH / 3;
        int y = HEIGHT / 3;
        Room chest = new Room(x, y, 2 * x, 2 * y, false);
        chest.makeCoins(numberGenerator);
        return chest;
    }

    /** Replays the previous session via reading SAVEFILE **/
    public void replay() {
        String input = Utils.readContentsAsString(SAVEFILE);
        Long seed = parseInput(input);
        if (seed == null) {
            drawFrame("No save found.", WIDTH / 2, HEIGHT / 2);
            StdDraw.pause(1500);
            System.exit(0);
            return;
        }
        TETile[][] world = makeTheWorld(seed);
        ter.renderFrame(world);
        String moveString = input.substring(input.indexOf('S') + 1);
        int i = 0;
        boolean lastColon = false;
        while (i < moveString.length()) {
            doHUD(world);
            StdDraw.pause(250);
            char c = moveString.charAt(i);
            if (c == ':') {
                inputString += c;
                lastColon = true;
            }
            boolean b = movementProcess(c, lastColon, world);
            if (!b) {
                showHUD("Replay complete!", world);
                return;
            }
            i++;
        }
    }

    /** X1 Makes walls according to room space **/
    public static void makeWalls(TETile[][] world) {
        for (int i = 0; i < world.length; i++) {
            for (int j = 0; j < world[0].length; j++) {
                if (world[i][j] == Tileset.WATER) {
                    Position p = new Position(i, j);
                    List<Position> surrounds = p.surroundingPositions();
                    for (Position s : surrounds) {
                        boolean xOnBorder = s.x() == 0 || s.x() == world.length - 1;
                        boolean yOnBorder = s.y() == 0 || s.y() == world[0].length - 1;
                        if (xOnBorder || yOnBorder) {
                            world[s.x()][s.y()] = Tileset.SAND;
                        } else if (inBounds(s, 0, WIDTH - 1, 0, HEIGHT - 1)) {
                            if (world[s.x()][s.y()] == Tileset.NOTHING) {
                                world[s.x()][s.y()] = Tileset.SAND;
                            }
                        }
                    }
                }
            }
        }
    }

    /** X1 Fills whatever that has not already been filled. **/
    public void fillRest(TETile[][] world) {
        for (int i = 0; i < world.length; i++) {
            for (int j = 0; j < world[0].length; j++) {
                if (world[i][j] == Tileset.NOTHING) {
                    world[i][j] = Tileset.MOUNTAIN;
                }
            }
        }
    }

    /** Asks for seed from user, returns it as a Long. **/
    public Long solicitSeed() {
        String s = "";
        drawFrame("Seed: " + s, WIDTH / 2, HEIGHT / 2);
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char c  = StdDraw.nextKeyTyped();
                if (c == 's' || c == 'S') {
                    StdDraw.setFont(SMALL_FONT);
                    return Long.parseLong(s);
                }
                s += Character.toString(c);
                drawFrame("Seed: " + s, WIDTH / 2, HEIGHT / 2);
            }
        }
    }

    /** Asks for name of the player from user, returns it as a String. **/
    public String solicitName() {
        String s = "";
        drawFrame("Name (Press # to submit): " + s, WIDTH / 2, HEIGHT / 2);
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char c  = StdDraw.nextKeyTyped();
                if (c == '#') {
                    StdDraw.setFont(SMALL_FONT);
                    return s;
                }
                s += Character.toString(c);
                drawFrame("Name: " + s, WIDTH / 2, HEIGHT / 2);
            }
        }
    }

    /** Asks for response to menu options, returns response as a char. **/
    public char solicitOption() {
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = StdDraw.nextKeyTyped();
                boolean upperCase = c == 'N' || c == 'L' || c == 'Q' || c == 'E' || c == 'R';
                boolean lowerCase = c == 'n' || c == 'l' || c == 'q' || c == 'e' || c == 'r';
                if (upperCase || lowerCase) {
                    return Character.toUpperCase(c);
                }
            }
        }
    }

    /** Method for moving interactively using the keyboard **/
    public boolean moveWithKeyBoard(TETile[][] world) {
        boolean lastColon = false;
        boolean dummy;
        while (true) {
            doHUD(world);
            if (StdDraw.hasNextKeyTyped()) {
                char c = StdDraw.nextKeyTyped();
                if (c == ':') {
                    inputString += c;
                    inputString += c;
                    dummy = true;
                } else {
                    dummy = false;
                }
                boolean b = movementProcess(c, lastColon, world);
                if (!b) {
                    System.exit(0);
                    return false;
                }
                lastColon = dummy;
            }
        }
    }

    /** Moves according to a given input string. **/
    public boolean moveWithText(String input, TETile[][] world) {
        int i = 0;
        boolean lastColon = false;
        boolean d;
        while (i < input.length()) {
            char c = input.charAt(i);
            if (c == ':') {
                inputString += c;
                d = true;
            } else {
                d = false;
            }
            boolean b = movementProcess(c, lastColon, world);
            if (!b) {
                return false;
            }
            lastColon = d;
            i++;
        }
        return false;
    }

    /** Takes a character and decides how the avatar should move **/
    public boolean movementProcess(char c, boolean lastColon, TETile[][] world) {
        boolean upperCase = c == 'W' || c == 'A' || c == 'S' || c == 'D';
        boolean lowerCase = c == 'w' || c == 'a' || c == 's' || c == 'd';
        if (upperCase || lowerCase) {
            c = Character.toUpperCase(c);
            inputString += c;
            moveAvatar(c, world);
        } else if ((c == 'Q' || c == 'q') && lastColon) {
            inputString += c;
            Utils.writeContents(SAVEFILE, inputString);
            return false;
        }
        return true;
    }

    /** Uses avatarPos to show where the avatar is **/
    public void updateAvatar(TETile[][] tiles) {
        tiles[avatarPos.x()][avatarPos.y()] = Tileset.AVATAR;
        ter.renderFrame(tiles);
    }

    /** Given a direction, decides the new position of the avatar **/
    public void moveAvatar(char c, TETile[][] world) {
        Position now = avatarPos;
        Position p = now;
        if (c == 'W') {
            p = now.newPositionShift(0, 1);
        } else if (c == 'A') {
            p = now.newPositionShift(-1, 0);
        } else if (c == 'S') {
            p = now.newPositionShift(0, -1);
        } else if (c == 'D') {
            p = now.newPositionShift(1, 0);
        }
        boolean inBounds = inBounds(p, 1, WIDTH - 2, 1, HEIGHT - 2);
        if (inBounds && world[p.x()][p.y()] == Tileset.WATER) {
            world[now.x()][now.y()] = Tileset.WATER;
            avatarPos = p;
            updateAvatar(world);
        }
    }

    /** Draws the frame **/
    public void drawFrame(String s, int x, int y) {
        StdDraw.clear(Color.BLACK);
        StdDraw.text(x, y, s);
        StdDraw.show();
    }

    /** Draws the world and the HUD **/
    public void showHUD(String s, TETile[][] world) {
        StdDraw.clear(Color.BLACK);
        ter.renderFrame(world);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.rectangle(25, 51, 25, 1);
        StdDraw.textLeft(1, 51, s);
        StdDraw.textRight(49, 51, avatarName);
        StdDraw.show();
    }

    /** Loads the world from the save file **/
    public TETile[][] loadWorld(boolean keyboard) {
        String in = Utils.readContentsAsString(SAVEFILE);
        if (in.length() < 2) {
            drawFrame("No saved world found.", WIDTH / 2, HEIGHT / 2);
            StdDraw.pause(1500);
            System.exit(0);
            return null;
        }
        if (keyboard) {
            in = in.substring(0, in.length() - 2);
        }
        return loadInteractInputString(in);
    }

    /** Returns position of mouse as a position object **/
    public Position mousePosition() {
        int x = (int) StdDraw.mouseX();
        int y = (int) StdDraw.mouseY();
        return new Position(x, y);
    }

    /** Finds mouse position, displays HUD **/
    public void doHUD(TETile[][] world) {
        Position p = mousePosition();
        if (inBounds(p, 0, 49, 0, 49)) {
            TETile t = world[p.x()][p.y()];
            String name = tileName(t);
            StdDraw.text(0, 0, name);
            showHUD(name, world);
        } else {
            StdDraw.text(0, 0, p.x() + " " + p.y());
        }
    }

    /** X1 Returns seed from input string. **/
    public Long parseInput(String input) {
        inputString += 'N';
        for (int i = 1; i < input.length(); i++) {
            char c = input.charAt(i);
            inputString += c;
            if (!Character.isDigit(c)) {
                String s = input.substring(1, i);
                return Long.parseLong(s);
            }
        }
        return null;
    }

    /** Returns the first index that is not a number in a string.
     * This should correspond to the end of the seed.
     */
    public int endSeedIndex(String input) {
        for (int i = 1; i < input.length(); i++) {
            char c = input.charAt(i);
            if (!Character.isDigit(c)) {
                return i;
            }
        }
        return -1;
    }

    /** X1 Finds random position within the world. **/
    public Position randomRoomPosition(TETile[][] world) {
        TETile t = Tileset.TREE;
        int x = -1, y = -1;
        int c = 0;
        while (t != Tileset.WATER) {
            if (c > 100) {
                return new Position(x, y);
            }
            x = numberGenerator.nextInt(WIDTH - 2) + 1;
            y = numberGenerator.nextInt(HEIGHT - 2) + 1;
            t = world[x][y];
            c++;
        }
        return new Position(x, y);
    }

    /** X1 Decides if a position is in bounds according to maxes and mins **/
    public static boolean inBounds(Position p, int xMin, int xMax, int yMin, int yMax) {
        if (p.x() < xMin || p.x() > xMax) {
            return false;
        }
        if (p.y() < yMin || p.y() > yMax) {
            return false;
        }
        return true;
    }

    /** Returns a message about tile t. **/
    public String tileName(TETile t) {
        if (t == Tileset.WATER) {
            return "Water. Splish Splash!";
        } else if (t == Tileset.SAND) {
            return "Sand. Perfect for making a sandcastle!";
        } else if (t == Tileset.MOUNTAIN) {
            return "Mountains. The type you can't climb.";
        } else if (t == Tileset.NOTHING) {
            return "Nothing. Welcome to the v o i d .";
        } else if (t == Tileset.AVATAR) {
            return "YOU! " + avatarName;
        } else if (t == Tileset.CHEST) {
            return "Buried treasure???";
        } else {
            return "Uhhhhhh idk tbh.";
        }
    }

    /** X1 Changes all positions in toChange in the world to the given tile **/
    public void change(TETile[][] tiles, List<Position> toChange, TETile tile) {
        for (Position c : toChange) {
            boolean inBounds = inBounds(c, 1, WIDTH - 1, 1, HEIGHT - 1);
            if (inBounds && tiles[c.x()][c.y()] == Tileset.NOTHING) {
                tiles[c.x()][c.y()] = tile;
            }
        }
    }

    /** Returns max of two numbers. Not sure why I wrote this TBH. **/
    public static int max(int x, int y) {
        if (x > y) {
            return x;
        }
        return y;
    }

    /** Returns min of two numbers. Not sure why I wrote this TBH. **/
    public static int min(int x, int y) {
        if (x < y) {
            return x;
        }
        return y;
    }

    /** Simple helper method to create a file. */
    public static void makeFile(File f) {
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** X1 NOT IN USE. Decides if a room is valid **/
    public static boolean isValid(List<Position> room, TETile[][] world) {
        if (room.size() == 0) {
            return false;
        }
        int x = 0;
        for (Position p : room) {
            if (!inBounds(p, 1, WIDTH - 2, 1, HEIGHT - 2)) {
                return false;
            }
            if (world[p.x()][p.y()] != Tileset.NOTHING) {
                x++;
            }
        }
        if (x > 5) {
            return false;
        }
        return true;
    }

    /** X1 NOT IN USE. Finds a random position in a room. **/
    public Position positionInRoom(List<Position> room) {
        int size = room.size();
        int x = numberGenerator.nextInt(size);
        return room.get(x);
    }
}
