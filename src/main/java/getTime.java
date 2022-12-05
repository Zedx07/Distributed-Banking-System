import java.rmi.Remote;
import java.rmi.RemoteException;

public interface getTime extends Remote {long getSystemTime() throws RemoteException;
}
