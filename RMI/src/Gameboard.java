import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public final class Gameboard extends UnicastRemoteObject implements Game {

    private static ConcurrentHashMap<Integer, Tank> tanks;
    private static CopyOnWriteArrayList<Bullet> bullets;
    private final AtomicInteger currentTID = new AtomicInteger();
    public final String name;

    public Gameboard(String name) throws RemoteException {
        this.name = name;
        tanks = new ConcurrentHashMap<>();
        bullets = new CopyOnWriteArrayList<>();
        currentTID.set(0);
    }

    @Override
    public String toString() {
        return "Gameboard: " + name;
    }

    public int register() throws RemoteException {
        int tankID;
        Tank tmp = null;
        do {
            tankID = currentTID.getAndIncrement();
            try {
                tmp = tanks.putIfAbsent(tankID, new Tank(tankID, this));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } while ( tmp != null );
        return tankID;
    }

    public RemoteTank getRemoteTank(int number) throws RemoteException {
        return tanks.get(number);
    }

    public void removeTank(int number) throws RemoteException {
        tanks.get(number).setAlive(false);
        tanks.remove(number);
    }

    public void addBullet(Bullet b) {
        bullets.add(b);
    }

    public List<Bullet> getBullets() throws RemoteException {
        return bullets;
    }

    public Map<Integer, Tank> getTanks(RemoteTank t) throws RemoteException {
//        Map<Integer, Tank> retMap = new HashMap<>();
//        Image[] enemy = new Image[4];
//        for(int i = 0; i < enemy.length; i++) {
//            enemy[i] = new ImageIcon("images/" + i + ".png").getImage();
//        }
//        tanks.forEach((k, v) -> {
//            try {
//                if (k != t.tankID()) {
//                    v.setTankImg(enemy);
//                    BufferedImage imageBuff = new BufferedImage(
//                            enemy[v.getDirection() - 1].getWidth(null),
//                            enemy[v.getDirection() - 1].getHeight(null),
//                            BufferedImage.TYPE_INT_RGB
//                    );
//                    imageBuff.createGraphics().drawImage(enemy[v.getDirection() - 1], 0, 0, null);
//                    v.setBuffImage(imageBuff);
//                }
//            } catch (RemoteException e) {
//                e.printStackTrace();
//            }
//            retMap.put(k, v);
//        });
//        return retMap;
        return tanks;
    }
}
