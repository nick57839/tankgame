import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface Game extends java.rmi.Remote {

  int register() throws RemoteException;
  RemoteTank getRemoteTank(int number) throws RemoteException;
  void removeTank(int number) throws RemoteException;
  Map<Integer, Tank> getTanks(RemoteTank t) throws RemoteException;
  List<Bullet> getBullets() throws RemoteException;
}
