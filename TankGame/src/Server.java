import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Server that runs socket and rmi servers.
 * Main class of the socket and rmi server.
 */
public class Server extends Thread {

    // socket variables
    private CopyOnWriteArrayList<ClientInfo> clients;
    private ServerSocket serverSocket;
    private int serverPort = 11111;
   
    private DataInputStream reader;
    private DataOutputStream writer;
   
    private Protocol protocol;
    private boolean running = true;
//    private static GameBoard gameBoard;

    // rmi variables
    public static final String GAME_NAME = "TankGame";
    private Registry registry;

    // used by both
    private static GameBoardInterface gameBoard;

    /**
     * Creates a socket and rmi server.
     * @throws SocketException necessary for socket server
     */
    public Server() throws SocketException {
        clients = new CopyOnWriteArrayList<>();
        try {
            gameBoard = new GameBoard(12, 12);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
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
                int id = 0;
                try {
                    id = gameBoard.insertTank();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
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
                try {
                    gameBoard.moveTank(point, Point.inFront(point, dir), id);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            else if(message.startsWith("Turn")) {
                int pos1 = message.indexOf(',');
                int pos2 = message.indexOf('-');
                int pos3 = message.indexOf('|');
                int x = Integer.parseInt(message.substring(4, pos1));
                int y = Integer.parseInt(message.substring(pos1 + 1, pos2));
                int dir = Integer.parseInt(message.substring(pos2 + 1, pos3));
                int id = Integer.parseInt(message.substring(pos3 + 1, message.length()));
                try {
                    gameBoard.turnTank(new Point(x, y), dir, id);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
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
                try {
                    gameBoard.insertBullet(point, id);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            else if(message.startsWith("Exit")) {
                int pos1 = message.indexOf(',');
                int pos2 = message.indexOf('|');
                int x = Integer.parseInt(message.substring(4, pos1));
                int y = Integer.parseInt(message.substring(pos1 + 1, pos2));
                int id = Integer.parseInt(message.substring(pos2 + 1, message.length()));
                try {
                    gameBoard.removeTank(new Point(x, y), id);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
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
     * Starts the socket and rmi server.
     * @return returns the port it is running on
     * @throws RemoteException possible exception
     * @throws AlreadyBoundException possible exception
     * @throws MalformedURLException possible exception
     */
    public synchronized int startServer() throws RemoteException, AlreadyBoundException, MalformedURLException {
        this.start();
        if (registry != null)
            throw new IllegalStateException("server already running");
        Registry reg = LocateRegistry.createRegistry(1091);
//        String bindLocation = "rmi://107.170.24.85:1091/" + GAME_NAME;
        String bindLocation = "rmi://localhost:1091/" + GAME_NAME;
        Naming.rebind(bindLocation, gameBoard);
        registry = reg;
        return 1091;
    }

    /**
     * Stops the socket and rmi server.
     * @throws IOException possible error when stopping server
     */
    public synchronized void stopServer() throws IOException {
        // stops socket server
        running = false;

        // stops RMI server
        if (registry != null) {
            try {
                registry.unbind(GAME_NAME);
            } catch (Exception e) {
                System.err.printf("unable to stop: %s%n", e.getMessage());
            } finally {
                registry = null;
            }
        }
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
                try {
                    int port = server.startServer();
                    System.out.printf("Socket server running on port %d%n", 11111);
                    System.out.printf("RMI server running on port %d%n", port);
                } catch (RemoteException e) {
                    Throwable t = e.getCause();
                    if (t instanceof java.net.ConnectException)
                        System.err.println("unable to connect to registry: " + t.getMessage());
                    else if (t instanceof java.net.BindException)
                        System.err.println("cannot start registry: " + t.getMessage());
                    else
                        System.err.println("cannot start server: " + e.getMessage());
                    UnicastRemoteObject.unexportObject(Server.gameBoard, false);
                } catch (AlreadyBoundException e) {
                    e.printStackTrace();
                }
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
