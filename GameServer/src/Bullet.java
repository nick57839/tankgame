import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Map;

/**
 * Bullet.java
 */
public class Bullet extends UnicastRemoteObject implements RemoteBullet{

    private final Image bulletImg;
    private final BufferedImage bulletBuffImage;
    private SerialImage serialImage;
    private final Tank theTank;
    
    private int xPosi;
    private int yPosi;
    private int direction;
    public boolean stop = false;
    private float velocityX = 0.05f, velocityY = 0.05f;
    
    public Bullet(int x, int y, int dir, Tank t) throws RemoteException {
        xPosi = x;
        yPosi = y;
        direction = dir;
        theTank = t;
        stop = false;
        bulletImg = new ImageIcon("images/bullet.png").getImage();
        bulletBuffImage = new BufferedImage(
                bulletImg.getWidth(null),
                bulletImg.getHeight(null),
                BufferedImage.TYPE_INT_RGB
        );
        bulletBuffImage.createGraphics().drawImage(bulletImg, 0, 0, null);
    }

    public synchronized int getPosiX() throws RemoteException {
        return xPosi;
    }
    public synchronized int getPosiY() throws RemoteException {
        return yPosi;
    }
    public synchronized void setPosiX(int x) {
        xPosi=x;
    }
    public synchronized void setPosiY(int y) {
        yPosi=y;
    }
    public synchronized BufferedImage getBulletBuffImg() {
        return bulletBuffImage;
    }
    public synchronized SerialImage getSerialImage() throws RemoteException {
        serialImage = new SerialImage(bulletBuffImage);
        return serialImage;
    }
    public synchronized boolean isStopped() throws RemoteException {
        return stop;
    }
    
    public boolean checkCollision() {
        Map<Integer, Tank> clientTanks = null;
        try {
            clientTanks = theTank.getGameboard().getTanks(theTank);
            int x, y;
            for (Tank tank : clientTanks.values()) {
                if (tank != null) {
                    x = tank.getXposition();
                    y = tank.getYposition();

                    if ((yPosi >= y && yPosi <= y + 43) && (xPosi >= x && xPosi <= x + 43)) {
                        theTank.setScore(100);
//                        theTank.getClientGUI().repaint();
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                        theTank.getGameboard().removeTank(tank.tankID());
                        return true;
                    }
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void startBulletThread(boolean chekCollision) {
        new BulletThread(chekCollision).start();
    }
    
    private class BulletThread extends Thread {
        boolean checkCollis;
        public BulletThread(boolean chCollision) {
            checkCollis = chCollision;
        }
        public void run() {
            if (checkCollis) {
                if (direction == 1) {
                    xPosi = 17 + xPosi;
                    while (yPosi > 50) {
                        yPosi = (int)(yPosi - yPosi * velocityY);
                        if(checkCollision())
                            break;
                        try {
                            Thread.sleep(40);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                } 
                else if (direction == 2) {
                    yPosi = 17 + yPosi;
                    xPosi += 30;
                    while (xPosi < 564) {
                        xPosi = (int)(xPosi + xPosi * velocityX);
                        if(checkCollision())
                            break;
                        try {
                            Thread.sleep(40);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                else if (direction == 3) {
                    yPosi += 30;
                    xPosi += 20;
                    while (yPosi < 505) {
                        yPosi = (int)(yPosi + yPosi * velocityY);
                        if (checkCollision())
                            break;
                        try {
                            Thread.sleep(40);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                else if (direction == 4) {
                    yPosi = 21 + yPosi;
                    while (xPosi > 70) {
                        xPosi = (int)(xPosi - xPosi * velocityX);
                        if (checkCollision())
                            break;
                        try {
                            Thread.sleep(40);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                stop = true;
            } 
            else {
                if (direction == 1) {
                    xPosi = 17 + xPosi;
                    while (yPosi > 50) {
                        yPosi = (int)(yPosi - yPosi * velocityY);
                        try {
                            Thread.sleep(40);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                else if (direction == 2) {
                    yPosi = 17 + yPosi;
                    xPosi += 30;
                    while (xPosi < 564) {
                        xPosi = (int)(xPosi + xPosi * velocityX);
                        try {
                            Thread.sleep(40);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                else if (direction == 3)
                {
                    yPosi += 30;
                    xPosi += 20;
                    while (yPosi < 505) {
                        yPosi = (int)(yPosi + yPosi * velocityY);
                        try {
                            Thread.sleep(40);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                else if (direction == 4) {
                    yPosi = 21 + yPosi;
                    while (xPosi > 70) {
                        xPosi = (int)(xPosi - xPosi * velocityX);
                        try {
                            Thread.sleep(40);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                stop = true;
            }
        }
    }
}
