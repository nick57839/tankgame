import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Thread class used to easily allow bullets to move themselves.
 */
public class Bullet extends Thread {

    private int id;
    private Point point;
    private int direction;
    private GameBoardInterface gameBoard;
    private boolean running = true;

    /**
     * Constructor
     * @param id id of the bullet
     * @param p point where the bullet is located on the gameboard
     * @param dir direction the bullet is traveling
     * @param g reference to the gameboard
     */
    public Bullet(int id, Point p, int dir, GameBoardInterface g) {
        this.id = id;
        point = p;
        direction = dir;
        gameBoard = g;
    }

    public Point getPoint() {
        return point;
    }
    public void setPoint(Point p) {
        point = p;
    }

    /**
     * Moves bullet on regular interval.
     * After bullet moves off the board regardless of what happened to it on the gameboard,
     * the thread will stop and then the bullet can be garbage collected.
     */
    public void run() {
        while (running) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Point next = Point.inFront(point, direction);
            try {
                if (next.x <= -2 || next.y <= -2
                        || next.x >= gameBoard.getWidth() + 2
                        || next.y >= gameBoard.getHeight() + 2)
                    running = false;
                else {
                    try {
                        gameBoard.moveBullet(point, next, id);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    point = next;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
