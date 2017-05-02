import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Tank extends UnicastRemoteObject implements RemoteTank {

    static final long serialVersionUID = 2709115310385083677L;
    private final int tankID;
    private Image[] tankImg;
    private BufferedImage imageBuff;
    private SerialImage serialImage;
    private int posiX = -1, posiY = -1;
    private int direction = 1;
    private int score = 0;
    private boolean alive = true;
    private final float velocityX = 0.03125f, velocityY = 0.03125f;
    private final int width = 559, height = 473;
//    private ClientGUI clientGUI;
    private final Gameboard gameboard;

    public Tank(int tid, Gameboard g) throws RemoteException {
        tankID = tid;
        gameboard = g;
        while (posiX < 70 | posiY < 50 | posiY > height - 43 | posiX > width - 43) {
            posiX = (int)(Math.random() * width);
            posiY = (int)(Math.random() * height);
        }
        tankImg = new Image[4];
        for(int i = 4; i < tankImg.length + 4; i++) {
            tankImg[i - 4] = new ImageIcon("images/" + i + ".png").getImage();
        }
        imageBuff = new BufferedImage(
                tankImg[direction - 1].getWidth(null),
                tankImg[direction - 1].getHeight(null),
                BufferedImage.TYPE_INT_RGB
        );
        imageBuff.createGraphics().drawImage(tankImg[direction - 1], 0, 0, null);
    }

    public synchronized BufferedImage getBuffImage() {
        return imageBuff;
    }
    public synchronized SerialImage getSerialImage() throws RemoteException {
        serialImage = new SerialImage(imageBuff);
        return serialImage;
    }
    public synchronized Image[] getTankImg() {
        return tankImg;
    }
    public synchronized void setBuffImage(BufferedImage bi) throws RemoteException {
        imageBuff = bi;
    }
    public synchronized void setTankImg(Image[] ti) {
        tankImg = ti;
    }
    public synchronized int getDirection() throws RemoteException {
        return direction;
    }
    public synchronized int getXposition() throws RemoteException {
        return posiX;
    }
    public synchronized int getYposition() throws RemoteException {
        return posiY;
    }
    private synchronized void setXposition(int x) {
        posiX = x;
    }
    private synchronized void setYposition(int y) {
        posiY = y;
    }
    public synchronized void setScore(int s) {
        score += s;
//        clientGUI.setScore(s);
    }
    public synchronized int getScore() throws RemoteException {
        return score;
    }
    public synchronized void setAlive(boolean a) {
        alive = a;
    }
    public synchronized boolean isAlive() throws RemoteException {
        return alive;
    }
//    public Bullet[] getBullet() throws RemoteException  {
//        return bullet;
//    }
//    public synchronized void setClientGUI( ClientGUI c) {
//        clientGUI = c;
//    }
//    public synchronized ClientGUI getClientGUI() {
//        return clientGUI;
//    }
    public Gameboard getGameboard() {
        return gameboard;
    }

    private boolean checkCollision(int xP, int yP) {
        try {
            for (Tank tank : gameboard.getTanks(this).values()) {
                if (tank != null) {
                    int x = tank.getXposition();
                    int y = tank.getYposition();
                    if (direction == 1)
                        return ((yP <= y + 43) && yP >= y)
                                && ((xP <= x + 43 && xP >= x) || (xP + 43 >= x && xP + 43 <= x + 43));
                    else if (direction == 2)
                        return ((xP + 43 >= x) && xP + 43 <= x + 43)
                                && ((yP <= y + 43 && yP >= y) || (yP + 43 >= y && yP + 43 <= y + 43));
                    else if (direction == 3)
                        return ((yP + 43 >= y) && yP + 43 <= y + 43)
                                && ((xP <= x + 43 && xP >= x) || (xP + 43 >= x && xP + 43 <= x + 43));
                    else if (direction == 4)
                        return ((xP <= x + 43) && xP >= x)
                                && ((yP <= y + 43 && yP >= y) || (yP + 43 >= y && yP + 43 <= y + 43));
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public int tankID() throws RemoteException {
        return tankID;
    }

    @Override
    public void moveLeft() {
        if (direction == 1 | direction == 3) {
            imageBuff = new BufferedImage(
                    tankImg[3].getWidth(null),
                    tankImg[3].getHeight(null),
                    BufferedImage.TYPE_INT_RGB
            );
            imageBuff.createGraphics().drawImage(tankImg[3],0,0,null);
            direction = 4;
        }
        else {
            int temp = (int)(posiX - velocityX * posiX);
            if(!checkCollision(temp, posiY) && temp < 70) {
                posiX = 70;
            }
            else if(!checkCollision(temp, posiY)) {
                posiX = temp;
            }
        }
    }

    @Override
    public void moveRight() {
        if( direction == 1 | direction == 3) {
            imageBuff = new BufferedImage(
                    tankImg[1].getWidth(null),
                    tankImg[1].getHeight(null),
                    BufferedImage.TYPE_INT_RGB
            );
            imageBuff.createGraphics().drawImage(tankImg[1],0,0,null);
            direction = 2;
        }
        else {
            int temp = (int)(posiX + velocityX * posiX);
            if(!checkCollision(temp, posiY) && temp > width - 23) {
                posiX = width - 23;
            }
            else if(!checkCollision(temp, posiY)) {
                posiX = temp;
            }
        }
    }

    @Override
    public void moveForward() {
        if(direction == 2 | direction == 4) {
            imageBuff = new BufferedImage(
                    tankImg[0].getWidth(null),
                    tankImg[0].getHeight(null),
                    BufferedImage.TYPE_INT_RGB
            );
            imageBuff.createGraphics().drawImage(tankImg[0],0,0,null);
            direction = 1;
        }
        else {
            int temp = (int)(posiY - velocityY * posiY);
            if(!checkCollision(posiX, temp) && temp < 50) {
                posiY = 50;
            }
            else if(!checkCollision(posiX, temp)) {
                posiY = temp;
            }
        }
    }

    @Override
    public void moveBackward() {
        if(direction == 2 | direction == 4) {
            imageBuff = new BufferedImage(
                    tankImg[2].getWidth(null),
                    tankImg[2].getHeight(null),
                    BufferedImage.TYPE_INT_RGB
            );
            imageBuff.createGraphics().drawImage(tankImg[2], 0, 0, null);
            direction = 3;
        }
        else {
            int temp = (int)(posiY + velocityY * posiY);
            if(!checkCollision(posiX, temp) && temp > height + 7) {
                posiY = height + 7;
            }
            else if(!checkCollision(posiX, temp)) {
                posiY = temp;
            }
        }
    }

    @Override
    public void shoot() throws RemoteException {
        Bullet bullet = new Bullet(this.getXposition(), this.getYposition(), direction, this);
        bullet.startBulletThread(true);
        gameboard.addBullet(bullet);
    }
}
