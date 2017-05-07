import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Client class used for sockets.
 */
public class Client {

    private Socket clientSocket;
    private String hostName;
    private int serverPort;
    private DataInputStream reader;
    private DataOutputStream writer;
    private Protocol protocol;

    private static Client client;
    private Client() throws IOException {
        protocol = new Protocol();
    }

    /**
     * Registers a data stream with the server.
     * @param Ip server ip
     * @param port server port
     * @throws IOException possible exception
     */
    public void register(String Ip, int port) throws IOException {
        serverPort = port;
        hostName = Ip;
        clientSocket = new Socket(Ip, port);
        writer = new DataOutputStream(clientSocket.getOutputStream());
        writer.writeUTF(protocol.RegisterPacket());
    }

    /**
     * Sends a message to the server.
     * @param message message to be sent to the server
     */
    public void sendToServer(String message)
    {   
        if (message.equals("exit"))
            System.exit(0);
        else {
             try {
                 Socket s = new Socket(hostName, serverPort);
//                 System.out.println(message);
                 writer = new DataOutputStream(s.getOutputStream());
                 writer.writeUTF(message);
            } catch (IOException ex) {}
        }
    }
    
    public Socket getSocket() {
        return clientSocket;
    }
    public String getIP() {
        return hostName;
    }

    /**
     * Creates a game client.
     * @return returns a new game client
     */
    public static Client getGameClient() {
        if(client == null)
            try {
                client = new Client();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        return client;
    }

    /**
     * Closes all client connections.
     */
    public void closeAll() {
        try {
            reader.close(); 
            writer.close();
            clientSocket.close();
        } catch (IOException ex) {}
    }
}
