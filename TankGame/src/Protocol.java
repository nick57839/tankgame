/**
 * Protocols used by the socket server and client
 */
public class Protocol {

    private String message = "";

    /**
     * The server sends the id of a new tank to a client.
     * @param id tank id
     * @return protocol in string form so it can be passed through sockets
     */
    public String IDPacket(int id) {
        message = "ID" + id;
        return message;
    }

    /**
     * The server sends the state of the gameboard to clients.
     * @param gameBoard gameboard
     * @return protocol in string form so it can be passed through sockets
     */
    public String GameStatePacket(GameBoardInterface gameBoard) {
        return "GameBoard\n" + gameBoard.toString();
    }

    /**
     * A client sends a request for a tank id.
     * @return protocol in string form so it can be passed through sockets
     */
    public String RegisterPacket() {
        message = "Hello";
        return message;
    }

    /**
     * A client sends a request for their tank to turn.
     * @param x current x location of the tank
     * @param y current y location of the tank
     * @param id the id of the tank
     * @param dir the direction the tank want to change to
     * @return protocol in string form so it can be passed through sockets
     */
    public String TurnPacket(int x, int y, int id, int dir) {
        message = "Turn" + x + "," + y + "-" + dir + "|" + id;
        return message;
    }

    /**
     * A client sends a request for their tank to move.
     * @param x current x location of the tank
     * @param y current y location of the tank
     * @param id the id of the tank
     * @param dir the current direction of the tank
     * @return protocol in string form so it can be passed through sockets
     */
    public String MovePacket(int x, int y, int id, int dir) {
        message = "Move" + x + "," + y + "-" + dir + "|" + id;
        return message;
    }

    /**
     * A client sends a request for their tank to shoot.
     * @param x current x location of the tank
     * @param y current y location of the tank
     * @param id the id of the tank
     * @param dir
     * @return protocol in string form so it can be passed through sockets
     */
    public String ShotPacket(int x, int y, int id, int dir) {
        message = "Shot" + x + "," + y + "-" + dir + "|" + id;
        return message;
    }

    /**
     * A client sends a message saying that the user has exited the program.
     * @param x current x location of the tank
     * @param y current y location of the tank
     * @param id the id of the tank
     * @return protocol in string form so it can be passed through sockets
     */
    public String ExitMessagePacket(int x, int y, int id) {
        message = "Exit" + x + "," + y + "|" + id;
        return message;
    }
}
