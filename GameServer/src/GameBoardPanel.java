import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * GameBoardPanel.java
 */
public class GameBoardPanel extends JPanel {

    private RemoteTank theTank;
    private Game game;
    private int width = 609;
    private int height = 523;
    private boolean gameStatus;
    private InputManager inputManager;
    public GameBoardPanel(RemoteTank tank, Game g, boolean gameStatus) {
        theTank = tank;
        game = g;
        this.gameStatus = gameStatus;
        setSize(width, height);
        setBounds(-50, 0, width, height);
        inputManager = new InputManager(tank);
        addKeyListener(inputManager);
        setFocusable(true);
    }

    public void setTank(RemoteTank rt) {
        theTank = rt;
        inputManager.setTank(rt);
    }

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
        if (gameStatus) {
            try {
                Image[] enemy = new Image[4];
                for (int i = 0; i < enemy.length; i++)
                    enemy[i] = new ImageIcon("images/" + i + ".png").getImage();
                for (RemoteTank tank : game.getTanks(theTank).values()) {
                    if(tank != null) {
                        if (tank.tankID() != theTank.tankID()) {
                            BufferedImage imageBuff = new BufferedImage(
                                    enemy[tank.getDirection() - 1].getWidth(null),
                                    enemy[tank.getDirection() - 1].getHeight(null),
                                    BufferedImage.TYPE_INT_RGB
                            );
                            imageBuff.createGraphics().drawImage(enemy[tank.getDirection() - 1], 0, 0, null);
                            g.drawImage(imageBuff, tank.getXposition(), tank.getYposition(), this);
                        }
                        else
                            g.drawImage(tank.getSerialImage().getBufferedImage(), tank.getXposition(), tank.getYposition(), this);
                    }
                }
                for (RemoteBullet bullet : game.getBullets()) {
                    if (bullet != null && !bullet.isStopped())
                        g.drawImage(bullet.getSerialImage().getBufferedImage(), bullet.getPosiX(), bullet.getPosiY(),this);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
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
