import java.awt.*;
import java.util.ArrayList;
import javax.swing.JPanel;
/**
 * GameBoardPanel.java
 */
public class GameBoardPanel extends JPanel {

    private Tank tank;
    private int width = 609;
    private int height = 523;
    private static ArrayList<Tank> tanks;
    private boolean gameStatus;
    public GameBoardPanel(Tank tank, Client client, boolean gameStatus) {
        this.tank = tank;
        this.gameStatus = gameStatus;
        setSize(width, height);
        setBounds(-50, 0, width, height);
        addKeyListener(new InputManager(tank));
        setFocusable(true);
        tanks = new ArrayList<>(100);
        for (int i = 0; i < 100; i++)
            tanks.add(null);
    }

    public void paintComponent(Graphics gr) {
        super.paintComponent(gr);
        Graphics2D g = (Graphics2D)gr;
 
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        
        g.setColor(Color.GREEN);
        g.fillRect(70, 50, getWidth() - 100, getHeight());
//        g.drawImage(new ImageIcon("images/bg.JPG").getImage(), 70, 50, null);
        g.setColor(Color.BLUE);
        g.setFont(new Font("Comic Sans MS", Font.BOLD, 25));
        g.drawString("2D Tank Game", 255, 30);
        if (gameStatus) {
            g.drawImage(tank.getBuffImage(), tank.getXposition(), tank.getYposition(), this);
            for(Bullet bullet : tank.getBullet()) {
                if(bullet != null && !bullet.stop)
                    g.drawImage(bullet.getBulletBuffImg(), bullet.getPosiX(), bullet.getPosiY(), this);
            }
            for(Tank tank : tanks) {
                if(tank != null) {
                    g.drawImage(tank.getBuffImage(), tank.getXposition(), tank.getYposition(), this);
                    for (Bullet bullet : tank.getBullet()) {
                        if (bullet != null && !bullet.stop)
                            g.drawImage(bullet.getBulletBuffImg(), bullet.getPosiX(), bullet.getPosiY(),this);
                    }
                }
            }
        }
        repaint();
        if (System.getProperty("os.name").equals("Linux"))
            Toolkit.getDefaultToolkit().sync();
    }

    public void registerNewTank(Tank newTank)
    {
        tanks.set(newTank.getTankID(), newTank);
    }
    public void removeTank(int tankID)
    {
        tanks.set(tankID, null);
    }
    public Tank getTank(int id)
    {
        return tanks.get(id);
    }
    public void setGameStatus(boolean status)
    {
        gameStatus = status;
    }
    public static ArrayList<Tank> getClients()
    {
        return tanks;
    }
}
