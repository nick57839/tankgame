import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Server that runs sockets.
 * Main class of the socket server.
 */
public class Server extends Thread {

    private CopyOnWriteArrayList<ClientInfo> clients;
    private ServerSocket serverSocket;
    private int serverPort = 11111;
   
    private DataInputStream reader;
    private DataOutputStream writer;
   
    private Protocol protocol;
    private boolean running = true;
    private static GameBoard gameBoard;

    /**
     * Creates a socket server.
     * @throws SocketException necessary for socket server
     */
    public Server() throws SocketException {
        clients = new CopyOnWriteArrayList<>();
        gameBoard = new GameBoard(12, 12);
        protocol = new Protocol();
        try {
            serverSocket = new ServerSocket(serverPort);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        new UpdateThread().start();
    }

    /**
     * Currently running server receives messages from clients and passes them to ProcessThread.
     * Further parallelism is possible.
     */
    public void run() {
        Socket clientSocket = null;
        while (running) {
            try {
                clientSocket = serverSocket.accept();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            String sentence = "";
            try {
                if (clientSocket != null)
                    reader = new DataInputStream(clientSocket.getInputStream());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            try {
                sentence = reader.readUTF();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            System.out.println(sentence);
            if (sentence.startsWith("Hello")) {
                try {
                    if (clientSocket != null)
                        writer = new DataOutputStream(clientSocket.getOutputStream());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                int id = gameBoard.insertTank();
                sendToClient(protocol.IDPacket(id));
                clients.add(new ClientInfo(writer, id));
            }
            else {
                new ProcessThread(sentence).start();
            }
        }
        try {
            reader.close();
            writer.close();
            serverSocket.close();
            if (clientSocket != null)
                clientSocket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Thread class that processes incoming messages from clients.
     */
    public class ProcessThread extends Thread {

        // message to be processed
        String message;

        public ProcessThread(String s) {
            message = s;
        }

        /**
         * Processes Move, Turn, Shot, or Exit message.
         * Messages are parsed based on special character dividers list in Protocol.
         */
        public void run() {
            if(message.startsWith("Move")) {
                int pos1 = message.indexOf(',');
                int pos2 = message.indexOf('-');
                int pos3 = message.indexOf('|');
                int x = Integer.parseInt(message.substring(4, pos1));
                int y = Integer.parseInt(message.substring(pos1 + 1, pos2));
                int dir = Integer.parseInt(message.substring(pos2 + 1, pos3));
                int id = Integer.parseInt(message.substring(pos3 + 1, message.length()));
                Point point = new Point(x, y);
                gameBoard.moveTank(point, Point.inFront(point, dir), id);
            }
            else if(message.startsWith("Turn")) {
                int pos1 = message.indexOf(',');
                int pos2 = message.indexOf('-');
                int pos3 = message.indexOf('|');
                int x = Integer.parseInt(message.substring(4, pos1));
                int y = Integer.parseInt(message.substring(pos1 + 1, pos2));
                int dir = Integer.parseInt(message.substring(pos2 + 1, pos3));
                int id = Integer.parseInt(message.substring(pos3 + 1, message.length()));
                gameBoard.turnTank(new Point(x, y), dir, id);
            }
            else if(message.startsWith("Shot")) {
                int pos1 = message.indexOf(',');
                int pos2 = message.indexOf('-');
                int pos3 = message.indexOf('|');
                int x = Integer.parseInt(message.substring(4, pos1));
                int y = Integer.parseInt(message.substring(pos1 + 1, pos2));
                int dir = Integer.parseInt(message.substring(pos2 + 1, pos3));
                int id = Integer.parseInt(message.substring(pos3 + 1, message.length()));
                Point point = new Point(x, y);
                int i = gameBoard.insertBullet(point, id);
                if (i != 0) {
                    new Bullet(i, Point.inFront(point, dir), dir, gameBoard).start();
                }
            }
            else if(message.startsWith("Exit")) {
                int pos1 = message.indexOf(',');
                int pos2 = message.indexOf('|');
                int x = Integer.parseInt(message.substring(4, pos1));
                int y = Integer.parseInt(message.substring(pos1 + 1, pos2));
                int id = Integer.parseInt(message.substring(pos2 + 1, message.length()));
                gameBoard.removeTank(new Point(x, y), id);
            }
        }
    }

    /**
     * Thread that consistently sends an updated board state to clients.
     */
    public class UpdateThread extends Thread {
        public void run() {
            while (running) {
                try {
                    Thread.sleep(100);
                    BroadCastMessage(new Protocol().GameStatePacket(gameBoard));
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Stops the server.
     * @throws IOException possible error when stopping server
     */
    public void stopServer() throws IOException {
        running = false;
    }

    /**
     * Used to send a message to all clients.
     * @param message message to be broadcast to all clients
     * @throws IOException possible exception
     */
    public void BroadCastMessage(String message) throws IOException {
        for (ClientInfo client : clients) {
            if (client != null)
                try {
                    client.getWriterStream().writeUTF(message);
                }
                catch (SocketException e) {
                    clients.remove(clients.indexOf(client));
                }
        }
    }

    /**
     * Can be used to send to a specific client.
     * @param message message to send to a client
     */
    public void sendToClient(String message) {
        if (message.equals("exit"))
            System.exit(0);
        else {
            try {
                writer.writeUTF(message);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Class containing reference to client stream and corresponding tank id.
     */
    public class ClientInfo {
        DataOutputStream writer;
        int tankID;
        
        public ClientInfo(DataOutputStream writer, int id) {
           this.writer = writer;
           tankID = id;
        }

        public int getTankID() {
            return tankID;
        }
        public void setTankID(int id) {
            tankID = id;
        }
        public DataOutputStream getWriterStream() {
            return writer;
        }
    }

    /**
     * Main
     * @param args Main arguments
     * @throws IOException possible exception
     */
    public static void main(String args[]) throws IOException {
        Server server = new Server();
        Scanner userInput = new Scanner(System.in);
        String input = "";
        boolean running = true;
        while (running) {
            System.out.println("Enter start or stop:");
            while (!userInput.hasNext());
            if (userInput.hasNext())
                input = userInput.nextLine();
            if (input.equals("start")) {
                server.start();
                System.out.println("Server is running.....");
            }
            else if (input.equals("stop")) {
                server.stopServer();
                running = false;
                System.out.println("Server is stopping.....");
            }
        }
        userInput.close();
        System.exit(0);
    }
}
