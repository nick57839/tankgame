import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.ImageIcon;
/**
 * Bullet.java
 */
public class Bullet {

    private Image bulletImg;
    private BufferedImage bulletBuffImage;
    
    private int xPosi;
    private int yPosi;
    private int direction;
    public boolean stop = false;
    private float velocityX = 0.05f, velocityY = 0.05f;
    
    public Bullet(int x, int y, int dir) {
        xPosi = x;
        yPosi = y;
        direction = dir;
        stop = false;
        bulletImg = new ImageIcon("images/bullet.png").getImage();
        bulletBuffImage = new BufferedImage(
                bulletImg.getWidth(null),
                bulletImg.getHeight(null),
                BufferedImage.TYPE_INT_RGB
        );
        bulletBuffImage.createGraphics().drawImage(bulletImg, 0, 0, null);
    }

    public int getPosiX() {
        return xPosi;
    }
    public int getPosiY() {
        return yPosi;
    }
    public void setPosiX(int x) {
        xPosi=x;
    }
    public void setPosiY(int y) {
        yPosi=y;
    }
    public BufferedImage getBulletBuffImg() {
        return bulletBuffImage;
    }
    
    public boolean checkCollision() {
        ArrayList<Tank> clientTanks = GameBoardPanel.getClients();
        int x, y;
        for (int i = 1; i < clientTanks.size(); i++) {
            if (clientTanks.get(i) != null) {
                x = clientTanks.get(i).getXposition();
                y = clientTanks.get(i).getYposition();
                
                if ((yPosi >= y && yPosi <= y + 43) && (xPosi >= x && xPosi <= x + 43)) {
                    ClientGUI.setScore(50);
                    ClientGUI.gameStatusPanel.repaint();
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    if(clientTanks.get(i)!=null)
                        Client.getGameClient().sendToServer(
                                new Protocol().RemoveClientPacket(clientTanks.get(i).getTankID())
                        );
                    return true;
                }
            }
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
