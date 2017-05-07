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
     * @return returns message to be sent to the server.
     */
    private String moveForward() {
        return new Protocol().MovePacket(
                ClientGUI.clientXPos,
                ClientGUI.clientYPos,
                ClientGUI.clientTank,
                ClientGUI.clientDir
        );
    }

    public void keyTyped(KeyEvent e) {}
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == LEFT) {
            if (ClientGUI.clientDir == 0 | ClientGUI.clientDir == 2) {
                client.sendToServer(
                        new Protocol().TurnPacket(
                                ClientGUI.clientXPos,
                                ClientGUI.clientYPos,
                                ClientGUI.clientTank,
                                3
                        )
                );
            }
            else if (ClientGUI.clientDir == 3) {
                client.sendToServer(moveForward());
            }
        }
        else if (e.getKeyCode() == RIGHT) {
            if (ClientGUI.clientDir == 0 | ClientGUI.clientDir == 2) {
                client.sendToServer(
                        new Protocol().TurnPacket(
                                ClientGUI.clientXPos,
                                ClientGUI.clientYPos,
                                ClientGUI.clientTank,
                                1
                        )
                );
            }
            else if (ClientGUI.clientDir == 1) {
                client.sendToServer(moveForward());
            }
        }
        else if (e.getKeyCode() == UP) {
            if (ClientGUI.clientDir == 1 | ClientGUI.clientDir == 3) {
                client.sendToServer(
                        new Protocol().TurnPacket(
                                ClientGUI.clientXPos,
                                ClientGUI.clientYPos,
                                ClientGUI.clientTank,
                                0
                        )
                );
            }
            else if (ClientGUI.clientDir == 0) {
                client.sendToServer(moveForward());
            }
        }
        else if (e.getKeyCode() == DOWN) {
            if (ClientGUI.clientDir == 1 | ClientGUI.clientDir == 3) {
                client.sendToServer(
                        new Protocol().TurnPacket(
                                ClientGUI.clientXPos,
                                ClientGUI.clientYPos,
                                ClientGUI.clientTank,
                                2
                        )
                );
            }
            else if (ClientGUI.clientDir == 2) {
                client.sendToServer(moveForward());
            }
        }
        else if(e.getKeyCode() == KeyEvent.VK_SPACE) {
            client.sendToServer(
                    new Protocol().ShotPacket(
                            ClientGUI.clientXPos,
                            ClientGUI.clientYPos,
                            ClientGUI.clientTank,
                            ClientGUI.clientDir
                    ));
        }
    }
    public void keyReleased(KeyEvent e) {}
}
