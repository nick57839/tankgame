import java.rmi.RemoteException;

/**
 * GameBoard shared state interface.
 */
public interface GameBoardInterface extends java.rmi.Remote {
    /**
     * move tank from a to b as long as b is clear of obstacles
     * @param a Point with x,y coordinates of the current location of the tank
     * @param b Point with the x,y coordinates of the location to move to
     * @param tid Unique identifier for the tank
     */
    void moveTank(Point a, Point b, int tid) throws RemoteException;

    /**
     * move bullet from a to b, calculate collisions
     * @param a Point of origin
     * @param b Destination point
     * @param bid Unique identifier of bullet
     */
    void moveBullet(Point a, Point b, int bid) throws RemoteException;

    /**
     * turn tank to requested direction
     * @param a point where tank is currently at
     * @param direction direction tank should be facing
     * @param tid unique identifier for tank
     */
    void turnTank(Point a, int direction, int tid) throws RemoteException;

    /**
     * adds tank to board at random location
     *
     * @return tankID of new tank
     */
    int insertTank() throws RemoteException;

    /**
     * removes a tank from the board
     * @param p point where tank is currently at
     * @param tid unique identifier of the tanks current location
     */
    void removeTank(Point p, int tid) throws RemoteException;

    /**
     * fires a bullet from the given tankID
     * @param p point where tank is currently at
     * @param tid unique identifier of the tanks current location
     * @return bulletID of new bullet
     */
    int insertBullet(Point p, int tid) throws RemoteException;

    /**
     * gets the current consistent board state
     *
     * @return array of ints representing the current board state
     */
    int[] readBoardState() throws RemoteException;

    /**
     * gets the current consistent board state as a string
     *
     * @return board state as a string
     */
    String gameState() throws RemoteException;

    int getWidth() throws RemoteException;
    int getHeight() throws RemoteException;
}
