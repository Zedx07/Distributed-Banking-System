
import java.time.Instant;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

// Remote object implementation that implements the remote interface
public class TimeServer extends UnicastRemoteObject implements getTime {
    public TimeServer() throws RemoteException {
        super();
        // call the constructor of the parent class
    }

    // method to get the time of the server implentated from the Remote interface
    public long getSystemTime() {
        long time = Instant.now().toEpochMilli(); // Instant is a class in java.time package
        System.out.println("Client request received at time(millisec) : " + time);
        return time;
    }

    public static void main(String[] args) {

        try {
            Registry reg = LocateRegistry.createRegistry(8080);
            // binding the remote object to the registry
            reg.rebind("timeServer", new TimeServer());
            System.out.println("Time Server is running...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}