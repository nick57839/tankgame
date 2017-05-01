import java.io.IOException;
import java.util.Scanner;

/**
 * Main.java
 */
public class Main {
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
        //new ServerGUI();
    }
}
