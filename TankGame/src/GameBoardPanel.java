import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * GameBoardPanel is the actual displayed gameboard not including the other displayed items.
 */
public class GameBoardPanel extends JPanel {

    private int width = 609;
    private int height = 557;
    private boolean gameStatus;
    private BufferedImage[] friendlyImage;
    private BufferedImage[] enemyImage;
    private BufferedImage bulletImage;
    private ClientGUI clientGUI;
    public GameBoardPanel(ClientGUI cgui, boolean gameStatus) {
        this.gameStatus = gameStatus;
        clientGUI = cgui;
        setSize(width, height);
        setBounds(-50, 0, width, height);
        addKeyListener(new InputManager());
        setFocusable(true);
        friendlyImage = new BufferedImage[4];
        for (int i = 4; i < friendlyImage.length + 4; i++) {
            try {
                friendlyImage[i - 4] = ImageIO.read(getClass().getResource("/images/" + i + ".png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        enemyImage = new BufferedImage[4];
        for (int i = 0; i < enemyImage.length; i++) {
            try {
                enemyImage[i] = ImageIO.read(getClass().getResource("/images/" + i + ".png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            bulletImage = ImageIO.read(getClass().getResource("/images/bullet.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Renders the gameboard.
     * @param gr graphics
     */
    public void paintComponent(Graphics gr) {
        super.paintComponent(gr);
        Graphics2D g = (Graphics2D)gr;
 
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        
        g.setColor(Color.GREEN);
        g.fillRect(70, 50, getWidth() - 100, getHeight());
//        g.drawImage(new ImageIcon("images/background.jpg").getImage(), 70, 50, null);
        g.setColor(Color.BLUE);
        g.setFont(new Font("Comic Sans MS", Font.BOLD, 25));
        g.drawString("2D Tank Game", 255, 30);
        if (gameStatus
                && ClientGUI.gameBoard != null
                && ClientGUI.clientTank.getId() != -1) {
            Scanner scanner = new Scanner(ClientGUI.gameBoard);
            boolean alive = false;
            for (int i = 0; i < 12; i++) {
                Scanner lineScanner = new Scanner(scanner.nextLine());
                for (int j = 0; j < 12; j++) {
                    int space = lineScanner.nextInt();
                    if (space != 0) {
                        if (space > 0) {
                            int objectID = GameBoard.decodeObjectID(space);
                            int objectDir = GameBoard.decodeDirection(space);
                            if (objectID != ClientGUI.clientTank.getId()) {
                                g.drawImage(enemyImage[objectDir], 70 + j * 42, 50 + i * 42, this);
                            } else {
                                g.drawImage(friendlyImage[objectDir], 70 + j * 42, 50 + i * 42, this);
                                ClientGUI.clientTank.setDir(objectDir);
                                ClientGUI.clientTank.setX(j);
                                ClientGUI.clientTank.setY(i);
                                alive = true;
                            }
                        }
                        else {
                            g.drawImage(bulletImage, 70 + j * 42, 50 + i * 42, this);
                        }
                    }
                }
            }
            if (!alive)
                clientGUI.isRunning = false;
        }
        repaint();
        if (System.getProperty("os.name").equals("Linux"))
            Toolkit.getDefaultToolkit().sync();
    }

    public void setGameStatus(boolean status)
    {
        gameStatus = status;
    }
}
