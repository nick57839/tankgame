import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.stream.IntStream;

/**
 * Created by chris on 5/2/17.
 */
public class GameBoard implements GameBoardInterface {
    public static final int NORTH = 0;
    public static final int EAST = 1;
    public static final int SOUTH = 2;
    public static final int WEST = 3;
    public static final boolean debug = true;

    private int height, width;
    private AtomicIntegerArray board;
    private int maxID = (int) Math.pow(2, 30);
    private AtomicInteger idCounter; // TODO: Counter never resets, for long games this will cause problems

    GameBoard(int h, int w) {
        height = h;
        width = w;
        board = new AtomicIntegerArray(width * height);
        idCounter = new AtomicInteger(1);
    }

    /**
     * move tank from a to b as long as b is clear of obstacles
     *
     * @param a   Point with x,y coordinates of the current location of the tank
     * @param b   Point with the x,y coordinates of the location to move to
     * @param tid Unique identifier for the tank
     */
    @Override
    public void moveTank(Point a, Point b, int tid) {
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
                return; // move completed successfully
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
    public void moveBullet(Point a, Point b, int bid) {
        int aIndex = calcIndex(a);
        int bIndex = calcIndex(b);

        while (true) {
            // read current state
            int aVal = board.get(aIndex);
            int bVal = board.get(bIndex);

            // validate A is the right bullet
            if (decodeObjectID(aVal) != bid) break;

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
                } else {
                    continue; // Something has move into our way, try again
                }
            } else {
                // destination is a tank or bullet, remove it and our self
                if (board.compareAndSet(bIndex, bVal, 0)) {
                    // removed B now cleanup our ghost
                    board.compareAndSet(aIndex, aVal, 0);
                    break;
                } else {
                    continue; // B has changed, try again
                }
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
    public void turnTank(Point a, int direction, int tid) {
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
    public int insertTank() {
        int tid = idCounter.getAndIncrement();
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
     * @param x
     * @param y
     * @param dir
     * @return
     */
    private int insertTank(int x, int y, int dir) {
        int tid = idCounter.getAndIncrement();
        int encoded = encodeState(tid, dir);

        int pIndex = calcIndex(x, y);
        board.set(pIndex, encoded);

        return tid;
    }

    /**
     * fires a bullet from the given tankID
     *
     * @param a current location of the tank
     * @param tid unique identifier of the tank
     * @return bulletID of new bullet
     */
    @Override
    public int insertBullet(Point a, int tid) {
        // get tank info
        int tankIndex = calcIndex(a);
        int aVal = board.get(tankIndex);
        int tankID = decodeObjectID(aVal);
        int tankDirection = decodeDirection(aVal);

        if (tankID != tid) return 0;

        // determine bullet location
        Point b = inFront(a, tankDirection);
        if (outOfBounds(b)) return 0;

        // create bullet
        int bid = idCounter.getAndIncrement();
        int encoded = encodeState(bid, tankDirection);
        int bulletIndex = calcIndex(b);

        do {
            int bVal = board.get(bulletIndex);

            if (bVal == 0) {
                if (board.compareAndSet(bulletIndex, bVal, encoded)) return bid;
            } else {
                // location is occupied, destroy object
                if (board.compareAndSet(bulletIndex, bVal, 0)) return 0;
            }
        } while (true);
    }

    /**
     * gets the current consistent board state
     *
     * @return array of ints representing the current board state
     */
    @Override
    public int[] readBoardState() {
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
                    .map(i -> decodeObjectID(i))
                    .count();

            distinctCount = Arrays.stream(copy)
                    .filter(i -> i != 0)
                    .map(i -> decodeObjectID(i))
                    .count();

        } while(count != distinctCount);

        return copy;
    }

    private int calcIndex(Point p) {
        return calcIndex(p.x, p.y);
    }

    private int calcIndex(int x, int y) {
        return width * x + y;
    }

    private int encodeState(int objectID, int dir) {
        assert(dir >= 0 && dir <= 3);
        assert(objectID < maxID);

        return (objectID << 2) | dir;
    }

    private int decodeObjectID(int state) {
        return state >>> 2;
    }

    private int decodeDirection(int state) {
        return state & 3;
    }

    private int randMax(int max) {
        return ThreadLocalRandom.current().nextInt(0, max);
    }

    /**
     * Determine the "front" side of an object
     * @param p current location of the object
     * @param dir orientation of object
     * @return A point directly in front of the object
     */
    private Point inFront(Point p, int dir) {
        switch( dir ) {
            case NORTH: return new Point(p.x-1, p.y);
            case SOUTH: return new Point(p.x+1, p.y);
            case EAST: return new Point(p.x, p.y+1);
            case WEST: return new Point(p.x, p.y-1);
            default: throw new IllegalArgumentException("default case should never be reached");
        }
    }

    private boolean outOfBounds(Point p) {
        if (p.x < 0 || p.x >= width || p.y < 0 || p.y >= height) return true;
        return false;
    }

    @Override
    public String toString() {
        StringBuffer s = new StringBuffer();
        int[] b = readBoardState();

        s.append(b[0] + " ");
        for(int i = 1; i < b.length; i++) {
            if (i % width == 0) s.append("\n");
            s.append(b[i] + " ");
        }

        return s.toString();
    }

    private void log(String s) {
        if (debug) System.out.println(s);
    }

    public static void main(String [] args) {
        GameBoard g = new GameBoard(4, 4);
        System.out.println(g + "\n");

        Point p1 = new Point(1,1);
        Point p2 = new Point(2,2);
        Point p3 = new Point(3,1);

        int t1 = g.insertTank(p1.x, p1.y, 1);
        int t2 = g.insertTank(p2.x, p2.y, 0);
        System.out.println(g + "\n");

        g.moveTank(p2, p3, t2);
        System.out.println(g + "\n");

        int b1 = g.insertBullet(p3, t2);
        System.out.println(g + "\n");

        g.turnTank(p3, 1, t2);
        System.out.println(g + "\n");

        g.moveBullet(new Point(2,1), new Point(1,1), b1);
        System.out.println(g + "\n");

        int b2 = g.insertBullet(p3, t2);
        System.out.println(g + "\n");
    }
}
