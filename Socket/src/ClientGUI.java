import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

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
    
    private JButton registerButton;
    
    private JPanel registerPanel;
    public static JPanel gameStatusPanel;
    private Client client;
    public static int clientTank;
    public static int clientXPos;
    public static int clientYPos;
    public static int clientDir;
    public static String gameBoard;
    
    private static int score;
    
    int width = 790, height = 580;
    boolean isRunning = true;
    private GameBoardPanel boardPanel;
    
    public ClientGUI() {
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
        registerPanel.setBounds(560, 50, 200, 140);
        registerPanel.setLayout(null);
        
        gameStatusPanel = new JPanel();
        gameStatusPanel.setBackground(Color.BLUE);
        gameStatusPanel.setSize(200, 300);
        gameStatusPanel.setBounds(560, 210, 200, 347);
        gameStatusPanel.setLayout(null);
     
        ipaddressLabel = new JLabel("IP Address: ");
        ipaddressLabel.setBounds(5, 25, 100, 25);
        
        portLabel = new JLabel("Port: ");
        portLabel.setBounds(5, 55, 50, 25);
        
        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setBounds(5, 90, 100, 25);
        
//        ipaddressText = new JTextField("107.170.24.85");
        ipaddressText = new JTextField("localhost");
        ipaddressText.setBounds(90, 25, 100, 25);
        
        portText = new JTextField("11111");
        portText.setBounds(90, 55, 100, 25);
       
        registerButton = new JButton("Register");
        registerButton.setBounds(45, 100, 120, 25);
        registerButton.addActionListener(this);
        registerButton.setFocusable(true);

        registerPanel.add(ipaddressLabel);
        registerPanel.add(portLabel);
        registerPanel.add(ipaddressText);
        registerPanel.add(portText);
        registerPanel.add(registerButton);
       
        gameStatusPanel.add(scoreLabel);
            
        client = Client.getGameClient();

        boardPanel = new GameBoardPanel(this, client, false);
        
        getContentPane().add(registerPanel);        
        getContentPane().add(gameStatusPanel);
        getContentPane().add(boardPanel);        
        setVisible(true);
    }
    
    public static int getScore() {
        return score;
    }
    
    public static void setScore(int scoreParamater) {
        score += scoreParamater;
        scoreLabel.setText("Score : " + score);
    }

    /**
     * Action listener for the Register button.
     * @param e action event
     */
    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();
        
        if (obj == registerButton) {
            registerButton.setEnabled(false);
            try {
                client.register(
                        ipaddressText.getText(),
                        Integer.parseInt(portText.getText())
                );
                boardPanel.setGameStatus(true);
                boardPanel.repaint();
                new ClientReceivingThread(client.getSocket()).start();
                registerButton.setFocusable(false);
                boardPanel.setFocusable(true);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "The Server is not running, try again later!",
                        "2D Tank Game",
                        JOptionPane.INFORMATION_MESSAGE);
                System.out.println("The Server is not running!");
                registerButton.setEnabled(true);
            }
        }
    }

    public void windowOpened(WindowEvent e) {}
    public void windowClosing(WindowEvent e) {
        // int response=JOptionPane.showConfirmDialog(this,"Are you sure you want to exit ?","Tanks 2D Multiplayer Game!",JOptionPane.YES_NO_OPTION);
        Client.getGameClient().sendToServer(new Protocol().ExitMessagePacket(clientXPos, clientYPos, clientTank));
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
                    sentence=reader.readUTF();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
//                System.out.println(sentence);
                if (sentence.startsWith("ID")) {
                    int id = Integer.parseInt(sentence.substring(2));
                    clientTank = id;
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
        }
    }

    /**
     * Command used to signal death of the tank to the client.
     * Currently not working.
     */
    public void death() {
        isRunning = false;
        int response = JOptionPane.showConfirmDialog(
                null,
                "Sorry, You lost. Do you want to try again?",
                "2D Tank Game",
                JOptionPane.OK_CANCEL_OPTION
        );
        if(response == JOptionPane.OK_OPTION) {
            //client.closeAll();
            setVisible(false);
            dispose();
            new ClientGUI();
        }
        else {
            System.exit(0);
        }
    }

    public static void main(String args[]) throws IOException {
        new ClientGUI();
    }
}
