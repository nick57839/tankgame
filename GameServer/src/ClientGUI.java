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
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * ClientGUI.java
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
    private RemoteTank clientTank;
    
    private static int score;
    
    int width = 790, height = 580;
    boolean isRunning = true;
    private GameBoardPanel boardPanel;
    private final Game game;
    
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
        registerPanel.setBounds(560, 50, 200, 140);
        registerPanel.setLayout(null);
        
        gameStatusPanel = new JPanel();
        gameStatusPanel.setBackground(Color.BLUE);
        gameStatusPanel.setSize(200, 300);
        gameStatusPanel.setBounds(560, 210, 200, 311);
        gameStatusPanel.setLayout(null);
     
        ipaddressLabel = new JLabel("IP Address: ");
        ipaddressLabel.setBounds(5, 25, 100, 25);
        
        portLabel = new JLabel("Port: ");
        portLabel.setBounds(5, 55, 50, 25);
        
        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setBounds(5, 90, 100, 25);
        
        ipaddressText = new JTextField("107.170.24.85");
        ipaddressText.setBounds(90, 25, 100, 25);
        
        portText = new JTextField("1091");
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

        String service = "//localhost:1091/TankGame";
        game = (Game) java.rmi.Naming.lookup(service);
        boardPanel = new GameBoardPanel(clientTank, game, false);
        
        getContentPane().add(registerPanel);        
        getContentPane().add(gameStatusPanel);
        getContentPane().add(boardPanel);
        setVisible(true);
    }
    
    public int getScore() {
        return score;
    }
    
    public void setScore(int scoreParamater) {
        score = scoreParamater;
        scoreLabel.setText("Score : " + score);
    }
    
    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();
        
        if (obj == registerButton) {
            registerButton.setEnabled(false);
            try {
                clientTank = game.getRemoteTank(game.register());
                boardPanel.setTank(clientTank);
                boardPanel.setGameStatus(true);
                boardPanel.repaint();
                new ClientUpdateThread().start();
                registerButton.setFocusable(false);
                boardPanel.setFocusable(true);
            } catch (IOException ex) {
                ex.printStackTrace();
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
        try {
            if (clientTank != null)
                game.removeTank(clientTank.tankID());
        } catch (RemoteException e1) {
            e1.printStackTrace();
        }
    }
    public void windowClosed(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowActivated(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}

    public class ClientUpdateThread extends Thread {

        public ClientUpdateThread() {}

        public void run() {
            while (isRunning) {
                try {
                    if (clientTank != null) {
                        if (clientTank.getScore() != score) {
                            score = clientTank.getScore();
                            gameStatusPanel.repaint();
                        }
                        if (!clientTank.isAlive()) {
                            int response = JOptionPane.showConfirmDialog(
                                    null,
                                    "Sorry, You lost. Do you want to try again?",
                                    "2D Tank Game",
                                    JOptionPane.OK_CANCEL_OPTION
                            );
                            if (response == JOptionPane.OK_OPTION) {
                                setVisible(false);
                                dispose();
                                clientTank = null;
                                new ClientGUI();
                            } else {
                                System.exit(0);
                            }
                        }
                    }
                    Thread.sleep(200);
                } catch (RemoteException | NotBoundException | MalformedURLException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
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
