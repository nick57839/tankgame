import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * GameBoard used as shared state of the game.
 */
public class GameBoard extends UnicastRemoteObject implements GameBoardInterface {
    public static final int NORTH = 0;
    public static final int EAST = 1;
    public static final int SOUTH = 2;
    public static final int WEST = 3;
    public static final boolean debug = true;

    private int height, width;
    private AtomicIntegerArray board;
    private int maxID = (int) Math.pow(2, 30);
    private AtomicInteger tankIDCounter; // TODO: Counter never resets, for long games this will cause problems
    private AtomicInteger bulletIDCounter; // TODO: Counter never resets, for long games this will cause problems

    public GameBoard(int h, int w) throws RemoteException {
        height = h;
        width = w;
        board = new AtomicIntegerArray(width * height);
        tankIDCounter = new AtomicInteger(1);
        bulletIDCounter = new AtomicInteger(1);
    }

    /**
     * move tank from a to b as long as b is clear of obstacles
     *
     * @param a   Point with x,y coordinates of the current location of the tank
     * @param b   Point with the x,y coordinates of the location to move to
     * @param tid Unique identifier for the tank
     */
    @Override
    public void moveTank(Point a, Point b, int tid) throws RemoteException {
        // if out of bounds ignore
        if (outOfBounds(b)) return;

        int aIndex = calcIndex(a);
        int bIndex = calcIndex(b);

        // read current state
        int aVal = board.get(aIndex);
        int bVal = board.get(bIndex);

        // validate A is a tank and B is empty
        if (decodeObjectID(aVal) != tid || bVal != 0) { log("failed sanity checks"); return; }

        // execute move operations
        if (board.compareAndSet(bIndex, bVal, aVal)) {
            if (board.compareAndSet(aIndex, aVal, 0)) {
                log("SUCCESS: " + tid);
                // move completed successfully
            } else {
                // bullet has hit at A, cleanup at B
                board.compareAndSet(bIndex, aVal, 0);
            }
        }

        // move failed, something has taken B while we were validating
    }

    /**
     * move bullet from a to b, calculate collisions
     *
     * @param a   Point of origin
     * @param b   Destination point
     * @param bid Unique identifier of bullet
     */
    @Override
    public void moveBullet(Point a, Point b, int bid) throws RemoteException {
        int aIndex = calcIndex(a);
        int bIndex = calcIndex(b);

        while (true) {
            // ignore invalid command
            if (outOfBounds(a)) break;

            // read current state
            int aVal = board.get(aIndex);

            // validate A is the right bullet
            if (-decodeObjectID(-aVal) != bid) break;

            // if bullet moving off board, remove it
            if (outOfBounds(b)) {
                board.compareAndSet(aIndex, aVal, 0);
                break;
            }

            // needs to come after bounds check
            int bVal = board.get(bIndex);

            // execute move, check for collisions
            if (bVal == 0) {
                log("b is clear, moving bullet " + bid);
                if (board.compareAndSet(bIndex, bVal, aVal)) {
                    if (board.compareAndSet(aIndex, aVal, 0)) break;
                    else {
                        // A has been hit by a bullet, cleanup
                        board.compareAndSet(bIndex, aVal, 0);
                        break;
                    }
                }
                // Something has move into our way, try again
            } else {
                // destination is a tank or bullet, remove it and our self
                if (board.compareAndSet(bIndex, bVal, 0)) {
                    // removed B now cleanup our ghost
                    board.compareAndSet(aIndex, aVal, 0);
                    break;
                }
                // B has changed, try again
            }
        }
    }

    /**
     * turn tank to requested direction
     *
     * @param a         point where tank is currently at
     * @param direction direction tank should be facing
     * @param tid       unique identifier for tank
     */
    @Override
    public void turnTank(Point a, int direction, int tid) throws RemoteException {
        int aIndex = calcIndex(a);
        int encoded = encodeState(tid, direction);

        do {
            int aVal = board.get(aIndex);
            if (decodeObjectID(aVal) != tid) break;
            if (board.compareAndSet(aIndex, aVal, encoded)) break;
        } while (true);
    }

    /**
     * adds tank to board at random location
     *
     * @return tankID of new tank
     *
     */
    @Override
    public int insertTank() throws RemoteException {
        int tid = tankIDCounter.getAndIncrement();
        int encoded = encodeState(tid, 0);

        // select a random point on the board and make sure its clear
        int pIndex, pVal;
        do {
           pIndex = calcIndex(randMax(width), randMax(height));
           pVal = board.get(pIndex);

           if (pVal != 0) continue; // try a different point

           if (board.compareAndSet(pIndex, pVal, encoded)) break;
        } while (true);

        return tid;
    }

    /**
     * Not thread safe, for debugging and testing purposes
     * @param x x coordinate to insert the tank at
     * @param y y coordinate to insert the tank at
     * @param dir direction to set the tank to initially
     * @return id of the tank
     */
    private int insertTank(int x, int y, int dir) {
        int tid = tankIDCounter.getAndIncrement();
        int encoded = encodeState(tid, dir);

        int pIndex = calcIndex(x, y);
        board.set(pIndex, encoded);

        return tid;
    }

    /**
     * removes a tank from the board
     * @param p point where tank is currently at
     * @param tid unique identifier of the tanks current location
     */
    @Override
    public void removeTank(Point p, int tid) throws RemoteException {
        int tankIndex = calcIndex(p);
        int aVal = board.get(tankIndex);
        int tankID = decodeObjectID(aVal);
        if (tankID != tid) return;
        board.compareAndSet(tankIndex, aVal, 0);
    }

    /**
     * fires a bullet from the given tankID
     *
     * @param a current location of the tank
     * @param tid unique identifier of the tank
     * @return bulletID of new bullet
     */
    @Override
    public int insertBullet(Point a, int tid) throws RemoteException {
        // get tank info
        int tankIndex = calcIndex(a);
        int aVal = board.get(tankIndex);
        int tankID = decodeObjectID(aVal);
        int tankDirection = decodeDirection(aVal);

        if (tankID != tid) return 0;

        // determine bullet location
        Point b = Point.inFront(a, tankDirection);
        if (outOfBounds(b)) return 0;

        // create bullet
        int bid = -bulletIDCounter.getAndIncrement();
        int encoded = -encodeState(-bid, tankDirection);
        int bulletIndex = calcIndex(b);

        do {
            int bVal = board.get(bulletIndex);

            if (bVal == 0) {
                if (board.compareAndSet(bulletIndex, bVal, encoded)) {
                    new Bullet(bid, b, tankDirection, this).start();
                    return bid;
                }
            } else {
                // location is occupied, destroy object
                if (board.compareAndSet(bulletIndex, bVal, 0))
                    return 0;
            }
        } while (true);
    }

    /**
     * gets the current consistent board state
     *
     * @return array of ints representing the current board state
     */
    @Override
    public int[] readBoardState() throws RemoteException {
        int[] copy;
        long count, distinctCount;
        do {
            // make a copy of board state
            copy = new int[board.length()];
            for(int i = 0; i < board.length(); i++) {
                copy[i] = board.get(i);
            }

            // check for duplicates which indicates the board is in the middle of a change
            count = Arrays.stream(copy)
                    .filter(i -> i != 0)
                    .map(GameBoard::decodeObjectID)
                    .count();

            distinctCount = Arrays.stream(copy)
                    .filter(i -> i != 0)
                    .map(GameBoard::decodeObjectID)
                    .count();

        } while(count != distinctCount);

        return copy;
    }

    private int calcIndex(Point p) {
        return calcIndex(p.x, p.y);
    }

    private int calcIndex(int x, int y) {
        return x + y * height;
    }

    private int encodeState(int objectID, int dir) {
        assert(dir >= 0 && dir <= 3);
        assert(objectID < maxID);

        return (objectID << 2) | dir;
    }

    public static int decodeObjectID(int state) {
        return state >>> 2;
    }

    public static int decodeDirection(int state) {
        return state & 3;
    }

    private int randMax(int max) {
        return ThreadLocalRandom.current().nextInt(0, max);
    }

    private boolean outOfBounds(Point p) {
        return (p.x < 0 || p.x >= width || p.y < 0 || p.y >= height);
    }

    @Override
    public int getWidth() throws RemoteException {
        return width;
    }

    @Override
    public int getHeight() throws RemoteException {
        return height;
    }

    @Override
    public String gameState() {
        StringBuilder s = new StringBuilder();
        int[] b = new int[0];
        try {
            b = readBoardState();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        s.append(b[0]).append(" ");
        for(int i = 1; i < b.length; i++) {
            if (i % width == 0) s.append("\n");
            s.append(b[i]).append(" ");
        }

        return s.toString();
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        int[] b = new int[0];
        try {
            b = readBoardState();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        s.append(b[0]).append(" ");
        for(int i = 1; i < b.length; i++) {
            if (i % width == 0) s.append("\n");
            s.append(b[i]).append(" ");
        }

        return s.toString();
    }

    private void log(String s) {
        if (debug) System.out.println(s);
    }

    public static void main(String [] args) {
        GameBoard g = null;
        try {
            g = new GameBoard(4, 4);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        System.out.println(g + "\n");

        Point p1 = new Point(1,1);
        Point p2 = new Point(2,2);
        Point p3 = new Point(3,1);

        int t1 = g.insertTank(p1.x, p1.y, 1);
        int t2 = g.insertTank(p2.x, p2.y, 0);
        System.out.println(g + "\n");

        try {
            g.moveTank(p2, p3, t2);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        System.out.println(g + "\n");

        try {
            g.turnTank(p3, 3, t2);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        System.out.println(g + "\n");

        int b1 = 0;
        try {
            b1 = g.insertBullet(p3, t2);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        System.out.println(g + "\n");

        try {
            g.moveBullet(new Point(2,1), new Point(1,1), b1);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        System.out.println(g + "\n");

        try {
            int b2 = g.insertBullet(p3, t2);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        System.out.println(g + "\n");
    }
}
