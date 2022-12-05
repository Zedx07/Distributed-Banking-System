import java.rmi.*;

// Remote interface for our getting time in application
public interface loadBalancerInterface extends Remote {

    public checkBal getServer() throws RemoteException;

    public int getServerName() throws RemoteException;

}
