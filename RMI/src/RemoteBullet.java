import java.rmi.RemoteException;

public interface RemoteBullet extends java.rmi.Remote {

  int getPosiX() throws RemoteException;
  int getPosiY() throws RemoteException;
  boolean isStopped() throws RemoteException;
  SerialImage getSerialImage() throws RemoteException;
}
