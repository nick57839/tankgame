import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * ClientGUI creates the graphical interface for the client.
 * Main class of the socket client.
 */
public class ClientGUI extends JFrame implements ActionListener, WindowListener {

    private JLabel ipaddressLabel;
    private JLabel portLabel;
    private static JLabel scoreLabel;
    
    private JTextField ipaddressText;
    private JTextField portText;
    
    private JButton socketButton;
    private JButton rmiButton;
    
    private JPanel registerPanel;
    public static JPanel gameStatusPanel;
    private Client client;
    public static Tank clientTank;
    public static String gameBoard;
    
    private static int score;
    
    int width = 790, height = 580;
    boolean isRunning = true;
    private GameBoardPanel boardPanel;

    public static GameBoardInterface game;
    public static boolean RMI = false;
    
    public ClientGUI() throws RemoteException, NotBoundException, MalformedURLException {
        score = 0;
        setTitle("2D Tank Game");
        setSize(width, height);
        setLocation(60, 100);
        getContentPane().setBackground(Color.BLACK);
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        addWindowListener(this);
        registerPanel = new JPanel();
        registerPanel.setBackground(Color.BLUE);
        registerPanel.setSize(200, 140);
        registerPanel.setBounds(560, 50, 200, 240);
        registerPanel.setLayout(null);
        
        gameStatusPanel = new JPanel();
        gameStatusPanel.setBackground(Color.BLUE);
        gameStatusPanel.setSize(200, 300);
        gameStatusPanel.setBounds(560, 320, 200, 237);
        gameStatusPanel.setLayout(null);
     
        ipaddressLabel = new JLabel("IP Address: ");
        ipaddressLabel.setBounds(5, 25, 100, 25);
        
        portLabel = new JLabel("Port: ");
        portLabel.setBounds(5, 55, 50, 25);
        
//        scoreLabel = new JLabel("Score: 0");
//        scoreLabel.setBounds(5, 90, 100, 25);
        
//        ipaddressText = new JTextField("107.170.24.85");
        ipaddressText = new JTextField("localhost");
        ipaddressText.setBounds(90, 25, 100, 25);
        
        portText = new JTextField("11111");
        portText.setBounds(90, 55, 100, 25);
       
        socketButton = new JButton("Socket");
        socketButton.setBounds(45, 120, 120, 25);
        socketButton.addActionListener(this);
        socketButton.setFocusable(true);

        rmiButton = new JButton("RMI");
        rmiButton.setBounds(45, 180, 120, 25);
        rmiButton.addActionListener(this);
        rmiButton.setFocusable(true);

        registerPanel.add(ipaddressLabel);
        registerPanel.add(portLabel);
        registerPanel.add(ipaddressText);
        registerPanel.add(portText);
        registerPanel.add(socketButton);
        registerPanel.add(rmiButton);
       
//        gameStatusPanel.add(scoreLabel);
            
        client = Client.getGameClient();
        clientTank = new Tank();

        boardPanel = new GameBoardPanel(this, false);
        
        getContentPane().add(registerPanel);        
        getContentPane().add(gameStatusPanel);
        getContentPane().add(boardPanel);        
        setVisible(true);
    }
    
    public static int getScore() {
        return score;
    }
    
    public static void setScore(int scoreParameter) {
        score += scoreParameter;
        scoreLabel.setText("Score : " + score);
    }

    /**
     * Action listener for the register buttons.
     * @param e action event
     */
    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();
        
        if (obj == socketButton) {
            socketButton.setEnabled(false);
            rmiButton.setEnabled(false);
            try {
                RMI = false;
                client.register(
                        ipaddressText.getText(),
                        Integer.parseInt(portText.getText())
                );
                new ClientReceivingThread(client.getSocket()).start();
                boardPanel.setGameStatus(true);
                boardPanel.repaint();
                socketButton.setFocusable(false);
                boardPanel.setFocusable(true);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "The Server is not running, try again later!",
                        "2D Tank Game",
                        JOptionPane.INFORMATION_MESSAGE);
                System.out.println("The Server is not running!");
                socketButton.setEnabled(true);
                rmiButton.setEnabled(true);
            }
        }
        else if (obj == rmiButton) {
            socketButton.setEnabled(false);
            rmiButton.setEnabled(false);
            try {
                RMI = true;
                //String service = "//107.170.24.85:1091/TankGame";
                String service = "//" + ipaddressText.getText() + ":"
                        + portText.getText() + "/TankGame";
                game = (GameBoardInterface) Naming.lookup(service);
                clientTank.setId(game.insertTank());
                new ClientUpdateThread().start();
                boardPanel.setGameStatus(true);
                boardPanel.repaint();
                rmiButton.setFocusable(false);
                boardPanel.setFocusable(true);
            } catch (IOException | NotBoundException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "The Server is not running, try again later!",
                        "2D Tank Game",
                        JOptionPane.INFORMATION_MESSAGE);
                System.out.println("The Server is not running!");
                socketButton.setEnabled(true);
                rmiButton.setEnabled(true);
            }
        }
    }

    public void windowOpened(WindowEvent e) {}
    public void windowClosing(WindowEvent e) {
        // int response=JOptionPane.showConfirmDialog(this,"Are you sure you want to exit ?","Tanks 2D Multiplayer Game!",JOptionPane.YES_NO_OPTION);
        cleanup();
    }
    public void windowClosed(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowActivated(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}

    /**
     * Thread class that receives and processes messages sent to the client.
     */
    public class ClientReceivingThread extends Thread {
        Socket clientSocket;
        DataInputStream reader;

        public ClientReceivingThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
            try {
                reader = new DataInputStream(clientSocket.getInputStream());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        /**
         * Process ID, Score, and GameBoard messages.
         */
        public void run() {
            while (isRunning) {
                String sentence = "";
                try {
                    sentence = reader.readUTF();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
//                System.out.println(sentence);
                if (sentence.startsWith("ID")) {
                    int id = Integer.parseInt(sentence.substring(2));
                    clientTank.setId(id);
                }
                else if (sentence.startsWith("Score")) {
                    int id = Integer.parseInt(sentence.substring(4));
                }
                else if (sentence.startsWith("GameBoard")) {
                    gameBoard = sentence.substring(sentence.indexOf('\n') + 1);
                    boardPanel.repaint();
                }
            }
            try {
                reader.close();
                clientSocket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            death();
        }
    }

    /**
     * Thread class that regularly updates the board for RMI
     */
    public class ClientUpdateThread extends Thread {

        /**
         * Retrieve game state from RMI server.
         */
        public void run() {
            while (isRunning) {
                try {
                    Thread.sleep(100);
                    gameBoard = game.gameState();
                } catch (InterruptedException | RemoteException e) {
                    e.printStackTrace();
                }
            }
            death();
        }
    }

    /**
     * Command used to signal death of the tank to the client.
     * Currently just exits, issue with gamestate messages being delivered after restarting.
     */
    public void death() {
        JOptionPane.showMessageDialog(
                this,
                "Sorry, Game Over",
                "2D Tank Game",
                JOptionPane.INFORMATION_MESSAGE);
        cleanup();
        System.exit(0);
//        int response = JOptionPane.showConfirmDialog(
//                null,
//                "Sorry, Game Over. Do you want to play again?",
//                "2D Tank Game",
//                JOptionPane.OK_CANCEL_OPTION
//        );
//        if(response == JOptionPane.OK_OPTION) {
//            //client.closeAll();
//            setVisible(false);
//            dispose();
//            new ClientGUI();
//        }
//        else {
//            Client.getGameClient().sendToServer(
//                    new Protocol().ExitMessagePacket(
//                            clientTank.getX(),
//                            clientTank.getY(),
//                            clientTank.getId()
//                    )
//            );
//            System.exit(0);
//        }
    }

    /**
     * Clean up helper method to make sure tank is gone when client closes
     */
    public void cleanup() {
        if (RMI) {
            try {
                game.removeTank(new Point(clientTank.getX(), clientTank.getY()), clientTank.getId());
            } catch (RemoteException e1) {
                e1.printStackTrace();
            }
        }
        else {
            Client.getGameClient().sendToServer(
                    new Protocol().ExitMessagePacket(
                            clientTank.getX(),
                            clientTank.getY(),
                            clientTank.getId()
                    )
            );
        }
    }

    public static void main(String args[]) throws IOException {
        try {
            new ClientGUI();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }
}
