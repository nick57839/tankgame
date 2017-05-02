import java.awt.image.BufferedImage;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;

public interface RemoteTank extends java.rmi.Remote {

  int tankID() throws RemoteException;
  void moveLeft() throws RemoteException;
  void moveRight() throws RemoteException;
  void moveForward() throws RemoteException;
  void moveBackward() throws RemoteException;
  void shoot() throws RemoteException;
  int getDirection() throws RemoteException;
//  Bullet[] getBullet() throws RemoteException;
  SerialImage getSerialImage() throws RemoteException;
  int getXposition() throws RemoteException;
  int getYposition() throws RemoteException;
}
