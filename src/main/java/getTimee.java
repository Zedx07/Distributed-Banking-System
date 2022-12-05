
import java.rmi.*;

// Remote interface for our getting time in application
public interface getTimee extends Remote {
    long getSystemTime() throws RemoteException;
}
