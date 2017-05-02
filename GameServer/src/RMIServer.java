import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RMIServer {

    public static final String GAME_NAME = "TankGame";

    private final Game game;
    private Registry registry;

    public RMIServer(Game game) {
        this.game = game;
    }

    public synchronized int start() throws RemoteException, AlreadyBoundException, MalformedURLException {
        if (registry != null)
            throw new IllegalStateException("server already running");
        Registry reg = LocateRegistry.createRegistry(1091);

//    reg.rebind(bank.name, bank);
        String bindLocation = "rmi://0.0.0.0:1091/" + RMIServer.GAME_NAME;
        Naming.bind(bindLocation, game);
        registry = reg;
        return 1091;
    }

    public synchronized void stop() {
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

    public static void main(String[] args) throws Exception {
        Gameboard gameboard = new Gameboard(GAME_NAME);
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
        }
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
    }
}
