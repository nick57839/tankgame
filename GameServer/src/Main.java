import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

/**
 * Main.java
 */
public class Main {
    public static void main(String args[]) throws IOException {
//        Server server = new Server();
//        Scanner userInput = new Scanner(System.in);
//        String input = "";
//        boolean running = true;
//        while (running) {
//            System.out.println("Enter start or stop:");
//            while (!userInput.hasNext());
//            if (userInput.hasNext())
//                input = userInput.nextLine();
//            if (input.equals("start")) {
//                server.start();
//                System.out.println("Server is running.....");
//            }
//            else if (input.equals("stop")) {
//                server.stopServer();
//                running = false;
//                System.out.println("Server is stopping.....");
//            }
//        }
//        userInput.close();
//        System.exit(0);

        Gameboard gameboard = new Gameboard(RMIServer.GAME_NAME);
        RMIServer server = new RMIServer(gameboard);
        try {
            int port = server.start();
            System.out.printf("server running on port %d%n", port);
        } catch (RemoteException e) {
            Throwable t = e.getCause();
            if (t instanceof java.net.ConnectException)
                System.err.println("unable to connect to registry: " + t.getMessage());
            else if (t instanceof java.net.BindException)
                System.err.println("cannot start registry: " + t.getMessage());
            else
                System.err.println("cannot start server: " + e.getMessage());
            UnicastRemoteObject.unexportObject(gameboard, false);
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        }
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
    }
}
