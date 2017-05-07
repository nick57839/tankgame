/**
 * Point class contains point in gameboard.
 */
public class Point {
    public int x, y;
    Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Determine the "front" side of an object
     * @param p current location of the object
     * @param dir orientation of object
     * @return A point directly in front of the object
     */
    public static Point inFront(Point p, int dir) {
        switch( dir ) {
            case GameBoard.NORTH: return new Point(p.x, p.y - 1);
            case GameBoard.SOUTH: return new Point(p.x, p.y + 1);
            case GameBoard.EAST: return new Point(p.x + 1, p.y);
            case GameBoard.WEST: return new Point(p.x - 1, p.y);
            default: throw new IllegalArgumentException("default case should never be reached");
        }
    }

    /**
     * Determine the "behind" side of an object
     * @param p current location of the object
     * @param dir orientation of object
     * @return A point directly behind the object
     */
    public static Point behind(Point p, int dir) {
        switch( dir ) {
            case GameBoard.NORTH: return new Point(p.x, p.y + 1);
            case GameBoard.SOUTH: return new Point(p.x, p.y - 1);
            case GameBoard.EAST: return new Point(p.x - 1, p.y);
            case GameBoard.WEST: return new Point(p.x + 1, p.y);
            default: throw new IllegalArgumentException("default case should never be reached");
        }
    }

    @Override
    public String toString() {
        return x + " : " + y;
    }
}
