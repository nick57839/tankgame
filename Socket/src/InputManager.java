import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

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
     * @return returns message to be sent to the server
     */
    private String moveForward() {
        return new Protocol().MovePacket(
                ClientGUI.clientTank.getX(),
                ClientGUI.clientTank.getY(),
                ClientGUI.clientTank.getId(),
                ClientGUI.clientTank.getDir()
        );
    }

    /**
     * Helper function used to send message to turn tank given direction.
     * @param dir direction to turn the tank
     * @return returns message to be sent to the server
     */
    private String turn(int dir) {
        return new Protocol().TurnPacket(
                ClientGUI.clientTank.getX(),
                ClientGUI.clientTank.getY(),
                ClientGUI.clientTank.getId(),
                dir
        );
    }

    public void keyTyped(KeyEvent e) {}
    public void keyPressed(KeyEvent e) {
        if (ClientGUI.clientTank != null && ClientGUI.clientTank.getId() != -1) {
            if (e.getKeyCode() == LEFT) {
                if (ClientGUI.clientTank.getDir() == GameBoard.NORTH
                        | ClientGUI.clientTank.getDir() == GameBoard.SOUTH) {
                    client.sendToServer(turn(GameBoard.WEST));
                } else if (ClientGUI.clientTank.getDir() == GameBoard.WEST) {
                    client.sendToServer(moveForward());
                }
            } else if (e.getKeyCode() == RIGHT) {
                if (ClientGUI.clientTank.getDir() == GameBoard.NORTH
                        | ClientGUI.clientTank.getDir() == GameBoard.SOUTH) {
                    client.sendToServer(turn(GameBoard.EAST));
                } else if (ClientGUI.clientTank.getDir() == GameBoard.EAST) {
                    client.sendToServer(moveForward());
                }
            } else if (e.getKeyCode() == UP) {
                if (ClientGUI.clientTank.getDir() == GameBoard.EAST
                        | ClientGUI.clientTank.getDir() == GameBoard.WEST) {
                    client.sendToServer(turn(GameBoard.NORTH));
                } else if (ClientGUI.clientTank.getDir() == GameBoard.NORTH) {
                    client.sendToServer(moveForward());
                }
            } else if (e.getKeyCode() == DOWN) {
                if (ClientGUI.clientTank.getDir() == GameBoard.EAST
                        | ClientGUI.clientTank.getDir() == GameBoard.WEST) {
                    client.sendToServer(turn(GameBoard.SOUTH));
                } else if (ClientGUI.clientTank.getDir() == GameBoard.SOUTH) {
                    client.sendToServer(moveForward());
                }
            } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                client.sendToServer(
                        new Protocol().ShotPacket(
                                ClientGUI.clientTank.getX(),
                                ClientGUI.clientTank.getY(),
                                ClientGUI.clientTank.getId(),
                                ClientGUI.clientTank.getDir()
                        ));
            }
        }
    }
    public void keyReleased(KeyEvent e) {}
}
