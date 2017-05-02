import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.rmi.RemoteException;

/**
 * InputManager.java
 */
public class InputManager implements KeyListener {

    private final int LEFT = 37;
    private  final int RIGHT = 39;
    private final int UP = 38;
    private final int DOWN = 40;
    private static int status = 0;
    
    private RemoteTank tank;
    public InputManager(RemoteTank tank) {
        this.tank = tank;
    }
    public void setTank(RemoteTank t) {
        tank = t;
    }

    public void keyTyped(KeyEvent e) {}
    public void keyPressed(KeyEvent e) {
        try {
            if (e.getKeyCode() == LEFT) {
                if (tank.getDirection() == 1 | tank.getDirection() == 3) {
                    tank.moveLeft();
                }
                else if (tank.getDirection() == 4) {
                    tank.moveLeft();
                }
            }
            else if (e.getKeyCode() == RIGHT) {
                if (tank.getDirection() == 1 | tank.getDirection() == 3) {
                    tank.moveRight();
                }
                else if (tank.getDirection() == 2) {
                    tank.moveRight();
                }
            }
            else if (e.getKeyCode() == UP) {
                if (tank.getDirection() == 2 | tank.getDirection() == 4) {
                    tank.moveForward();
                }
                else if (tank.getDirection() == 1) {
                    tank.moveForward();
                }
            }
            else if (e.getKeyCode() == DOWN) {
                if (tank.getDirection() == 2 | tank.getDirection() == 4) {
                    tank.moveBackward();
                }
                else if (tank.getDirection() == 3) {
                    tank.moveBackward();
                }
            }
            else if(e.getKeyCode() == KeyEvent.VK_SPACE) {
                    tank.shoot();
            }
        } catch (RemoteException e1) {
            e1.printStackTrace();
        }
    }
    public void keyReleased(KeyEvent e) {}
}
