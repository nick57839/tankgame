import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.ImageIcon;
/**
 * Tank.java
 */
public class Tank {
    
    private Image[] tankImg;
    private BufferedImage ImageBuff;
    private Bullet bullet[] = new Bullet[100];
    private int curBomb = 0;
    private int tankID;
    private int posiX = -1, posiY = -1;
    private int direction = 1;
    private float velocityX = 0.03125f, velocityY = 0.03125f;
    private int width = 559, height = 473;

    public int getDirection() {
        return direction;
    }

    public Tank() {
        while (posiX < 70 | posiY < 50 | posiY > height - 43 | posiX > width - 43) {
            posiX = (int)(Math.random() * width);
            posiY = (int)(Math.random() * height);
        }
        loadImage(4);
    }

    public Tank(int x, int y, int dir, int id) {
        posiX = x;
        posiY = y;
        tankID = id;
        direction = dir;
        loadImage(0);
    }

    public void loadImage(int a) {
        tankImg = new Image[4];
        for(int i = a; i < tankImg.length + a; i++) {
            tankImg[i-a]=new ImageIcon("images/" + i + ".png").getImage();
        }
        ImageBuff = new BufferedImage(
                tankImg[direction - 1].getWidth(null),
                tankImg[direction - 1].getHeight(null),
                BufferedImage.TYPE_INT_RGB
        );
        ImageBuff.createGraphics().drawImage(tankImg[direction - 1], 0, 0, null);
    }

    public BufferedImage getBuffImage() {
        return ImageBuff;
    }
    public int getXposition() {
        return posiX;
    }
    public int getYposition() {
        return posiY;
    }
    public void setXpoistion(int x) {
        posiX = x;
    }
    public void setYposition(int y) {
        posiY = y;
    }

    public void moveLeft() {
        if (direction == 1 | direction == 3) {
           ImageBuff = new BufferedImage(
                   tankImg[3].getWidth(null),
                   tankImg[3].getHeight(null),
                   BufferedImage.TYPE_INT_RGB
           );
           ImageBuff.createGraphics().drawImage(tankImg[3],0,0,null);
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

    public void moveRight() {
        if( direction == 1 | direction == 3) {
           ImageBuff = new BufferedImage(
                   tankImg[1].getWidth(null),
                   tankImg[1].getHeight(null),
                   BufferedImage.TYPE_INT_RGB
           );
           ImageBuff.createGraphics().drawImage(tankImg[1],0,0,null);
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

    public void moveForward() {
        if(direction == 2 | direction == 4) {
           ImageBuff = new BufferedImage(
                   tankImg[0].getWidth(null),
                   tankImg[0].getHeight(null),
                   BufferedImage.TYPE_INT_RGB
           );
           ImageBuff.createGraphics().drawImage(tankImg[0],0,0,null);
           direction = 1;
        }
        else {
            int temp = (int)(posiY - velocityY * posiY);
            if(!checkCollision(posiX, temp) && temp < 50) {
                posiY = 50;
            }
            else if(!checkCollision(posiX, temp)) {
                posiY=temp;
            }
        }
    }

    public void moveBackward() {
        if(direction == 2 | direction == 4) {
           ImageBuff = new BufferedImage(
                   tankImg[2].getWidth(null),
                   tankImg[2].getHeight(null),
                   BufferedImage.TYPE_INT_RGB
           );
           ImageBuff.createGraphics().drawImage(tankImg[2], 0, 0, null);
           direction = 3;
        }
        else {
            int temp = (int)(posiY + velocityY * posiY);
            if(!checkCollision(posiX, temp) && temp > height + 7) {
              posiY = height + 7;
            }
            else if(!checkCollision(posiX, temp)) {
                posiY=temp;
            } 
        }
    }
    
    public void shot() {
        bullet[curBomb] = new Bullet(this.getXposition(), this.getYposition(), direction);
        bullet[curBomb].startBombThread(true);
        curBomb++;
    }

    public Bullet[] getBullet()
    {
        return bullet;
    }
    public void setTankID(int id)
    {
        tankID = id;
    }
    public int getTankID()
    {
        return tankID;
    }

    public void setDirection(int dir) {
        ImageBuff = new BufferedImage(
                tankImg[dir - 1].getWidth(null),
                tankImg[dir - 1].getHeight(null),
                BufferedImage.TYPE_INT_RGB
        );
        ImageBuff.createGraphics().drawImage(tankImg[dir - 1],0,0,null);
        direction = dir;
    }

    public void Shot() {
        bullet[curBomb] = new Bullet(this.getXposition(), this.getYposition(), direction);
        bullet[curBomb].startBombThread(false);
        curBomb++;
    }

    public boolean checkCollision(int xP, int yP) {
        ArrayList<Tank> clientTanks = GameBoardPanel.getClients();
        int x, y;
        for (int i = 1; i < clientTanks.size(); i++) {
            if (clientTanks.get(i) != null) {
                x = clientTanks.get(i).getXposition();
                y = clientTanks.get(i).getYposition();
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
        return false;
    }
}
