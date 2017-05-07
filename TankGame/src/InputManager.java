import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.rmi.RemoteException;

/**
 * InputManager listens for key input.
 */
public class InputManager implements KeyListener {

    private final int LEFT = 37;
    private  final int RIGHT = 39;
    private final int UP = 38;
    private final int DOWN = 40;

    private Client client;

    public InputManager() {
        this.client = Client.getGameClient();
    }

    /**
     * Helper function used to send message to move tank forward regardless of direction.
     */
    private void moveForward() {
        if (ClientGUI.RMI) {
            Point current = new Point(ClientGUI.clientTank.getX(), ClientGUI.clientTank.getY());
            try {
                ClientGUI.game.moveTank(
                        current,
                        Point.inFront(current, ClientGUI.clientTank.getDir()),
                        ClientGUI.clientTank.getId()
                );
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        else {
            client.sendToServer(new Protocol().MovePacket(
                    ClientGUI.clientTank.getX(),
                    ClientGUI.clientTank.getY(),
                    ClientGUI.clientTank.getId(),
                    ClientGUI.clientTank.getDir()
            ));
        }
    }

    /**
     * Helper function used to send message to turn tank given direction.
     * @param dir direction to turn the tank
     */
    private void turn(int dir) {
        if (ClientGUI.RMI) {
            Point current = new Point(ClientGUI.clientTank.getX(), ClientGUI.clientTank.getY());
            try {
                ClientGUI.game.turnTank(
                        current,
                        dir,
                        ClientGUI.clientTank.getId()
                );
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        else {
            client.sendToServer(new Protocol().TurnPacket(
                    ClientGUI.clientTank.getX(),
                    ClientGUI.clientTank.getY(),
                    ClientGUI.clientTank.getId(),
                    dir
            ));
        }
    }

    /**
     * Helper function used to send message for tank to shoot.
     */
    private void shoot() {
        if (ClientGUI.RMI) {
            Point current = new Point(ClientGUI.clientTank.getX(), ClientGUI.clientTank.getY());
            try {
                ClientGUI.game.insertBullet(
                        current,
                        ClientGUI.clientTank.getId()
                );
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        else {
            client.sendToServer(new Protocol().ShotPacket(
                    ClientGUI.clientTank.getX(),
                    ClientGUI.clientTank.getY(),
                    ClientGUI.clientTank.getId(),
                    ClientGUI.clientTank.getDir()
            ));
        }
    }

    public void keyTyped(KeyEvent e) {}
    public void keyPressed(KeyEvent e) {
        if (ClientGUI.clientTank != null && ClientGUI.clientTank.getId() != -1) {
            if (e.getKeyCode() == LEFT) {
                if (ClientGUI.clientTank.getDir() == GameBoard.NORTH
                        | ClientGUI.clientTank.getDir() == GameBoard.SOUTH) {
                    turn(GameBoard.WEST);
                } else if (ClientGUI.clientTank.getDir() == GameBoard.WEST) {
                    moveForward();
                }
            } else if (e.getKeyCode() == RIGHT) {
                if (ClientGUI.clientTank.getDir() == GameBoard.NORTH
                        | ClientGUI.clientTank.getDir() == GameBoard.SOUTH) {
                    turn(GameBoard.EAST);
                } else if (ClientGUI.clientTank.getDir() == GameBoard.EAST) {
                    moveForward();
                }
            } else if (e.getKeyCode() == UP) {
                if (ClientGUI.clientTank.getDir() == GameBoard.EAST
                        | ClientGUI.clientTank.getDir() == GameBoard.WEST) {
                    turn(GameBoard.NORTH);
                } else if (ClientGUI.clientTank.getDir() == GameBoard.NORTH) {
                    moveForward();
                }
            } else if (e.getKeyCode() == DOWN) {
                if (ClientGUI.clientTank.getDir() == GameBoard.EAST
                        | ClientGUI.clientTank.getDir() == GameBoard.WEST) {
                    turn(GameBoard.SOUTH);
                } else if (ClientGUI.clientTank.getDir() == GameBoard.SOUTH) {
                    moveForward();
                }
            } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                shoot();
            }
        }
    }
    public void keyReleased(KeyEvent e) {}
}
