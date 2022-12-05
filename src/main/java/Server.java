
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLOutput;
import java.util.ArrayList;

public class Server extends UnicastRemoteObject implements checkBal {
    static MongoClient client = MongoClients
            .create("mongodb+srv://Shubham:1234@cluster0.srmnd6r.mongodb.net/?retryWrites=true&w=majority");
    //first db
    static MongoDatabase db = client.getDatabase("BD");
    static MongoCollection coll = db.getCollection("coll_BD");
    //second db
    static MongoDatabase another_db = client.getDatabase("Another_BD");
    static MongoCollection another_coll = another_db.getCollection("Another_coll_BD");

    public static void addOnDatabase() {

        Document sample = new Document("AccountNo", "1").append("Password", "password1").append("Balance", "2000.0");

        coll.insertOne(sample);

    }

    // static Document doc;
    public static Document getFromDatabase(int accNo) {

        Document doc = (Document) coll.find(new Document("AccountNo", accNo)).first();
        return doc;
    }

    // update balance on database
    public static void updateBalance(int accNo, double balance) {
        Document doc = getFromDatabase(accNo);
        System.out.println("doc");
        System.out.println(doc);
        UpdateResult result = coll.updateOne(doc, Updates.set("Balance", balance));

        doc = getFromDatabase(accNo);
        System.out.println("doc from another db");
        System.out.println(doc);
        result = another_coll.updateOne(doc, Updates.set("Balance", balance));
    }

    public static Document getFromAnotherDatabase(int accNo) {
        Document doc = (Document) another_coll.find(new Document("AccountNo", accNo)).first();
        return doc;
    }

    // update balance on another_database
    public static void updateAnotherBalance(int accNo, double balance) {
        Document doc = getFromDatabase(accNo);
        System.out.println("doc from another db");
        System.out.println(doc);
        UpdateResult result = another_coll.updateOne(doc, Updates.set("Balance", balance));
        System.out.println("result: "+result);

    }
    // public static void updateOnDatabase(int accNo, int bal){

    // // UpdateResult doc = coll.updateOne(new Document("AccountNo", accNo),new
    // Document("$set", new Document("Balance", bal)));
    // UpdateResult doc = coll.updateOne(Filters.eq("AccountNo", accNo),
    // Updates.set("Balance", bal));

    // }

    public Server(int serverNo) throws RemoteException {
        super();
        RN = new int[3];
        no_of_requests = 0;
        exec = 0;
        critical = false;
        this.serverNo = serverNo;
        try {
            Registry reg = LocateRegistry.getRegistry("localhost", 8082);
            TokenInterface token = (TokenInterface) reg.lookup("tokenServer");
            this.token = token;
        } catch (Exception e) {
            System.out.println("Exception occurred : " + e.getMessage());
        }
    }

    static ArrayList<Account> a = new ArrayList<Account>() {
        {
            add(new Account("123456", "password1", 2000));
            add(new Account("456789", "password2", 3300));
            add(new Account("234567", "password3", 4000));
            add(new Account("345678", "password4", 5000));
        }
    };

    static ArrayList<Account> b = new ArrayList<Account>() {
        {
            add(new Account("123456", "password1", 2000));
            add(new Account("456789", "password2", 3300));
            add(new Account("234567", "password3", 4000));
            add(new Account("345678", "password4", 5000));
        }
    };
    int RN[], exec;
    boolean critical;
    int no_of_requests;
    TokenInterface token;
    int serverNo;

    public int checkBalance(String acc_no, String password) throws RemoteException {
        try {
            if (exec == 1) {
                throw new Exception(" ");
            }
            System.out.println("Balance request received for account number " + acc_no);
            for (int i = 0; i < a.size(); i++) {
                int bal = a.get(i).checkBalance(acc_no, password);

                if (bal != -1)
                    return bal;
            }
            return -1;
        } catch (Exception e) {
            System.out.println(e.getMessage() + "\nCannot access datastore 1\nTrying to access backup datastore");
            for (int i = 0; i < b.size(); i++) {
                int bal = b.get(i).checkBalance(acc_no, password);
                if (bal != -1)
                    // exec = 0; for redirectiong to datastore 1
                    return bal;
            }
            return -1;
        }
    }

    public boolean transfer(String d_acc_no, String cred_acc_no, String password, int amt) throws RemoteException {
        System.out.println("Transfer request received for account number " + d_acc_no);
        System.out.println("Transfer to credit account number " + cred_acc_no);
        boolean isValid = false;
        try {
            if (exec == 1) {
                throw new Exception(" ");
            }
            for (int i = 0; i < a.size(); i++) {
                isValid = a.get(i).checkValid(d_acc_no, password);
                if (isValid) {
                    break;
                }
            }
        } catch (Exception e) {
            // System.out.println(e.getMessage()+"\nCannot access datastore 1\nTrying to
            // access datastore 2");
            for (int i = 0; i < b.size(); i++) {
                isValid = b.get(i).checkValid(d_acc_no, password);
                if (isValid) {
                    break;
                }
            }
        }

        if (!isValid) {
            return false;
        } else {
            if (token.getOwner() == -1) {
                token.setOwner(serverNo);
                System.out.println("No owner");
                no_of_requests++;
                RN[serverNo]++;
            } else {
                sendRequest(); //
            }
            while (token.getOwner() != serverNo)
                ;
            System.out.println("Got token");
            critical = true;
//            System.out.println(d_acc_no.getClass().getName());
//            System.out.println(cred_acc_no.getClass().getName());
//            System.out.println(password.getClass().getName());
////            System.out.println(amt.getClass().getName());
//            System.out.println(d_acc_no);
//            System.out.println(cred_acc_no);
//            System.out.println(password);
//            System.out.println(amt);


//            d_acc_no.getClass().getName();
            boolean b = critical_section(d_acc_no, cred_acc_no, password, amt);
            critical = false;
            releaseToken();
            return b;
        }
    }

    public void sendRequest() throws RemoteException {
        no_of_requests++;
        for (int i = 0; i < 3; i++) {
            try {
                Registry reg = LocateRegistry.getRegistry("localhost",
                        8000 + i);
                checkBal server = (checkBal) reg.lookup("bankServer" + i);
                server.receiveRequest(serverNo, no_of_requests);
            } catch (Exception e) {
                System.out.println("Exception occurred : " +
                        e.getMessage());
            }
        }
    }

    public void getAnother(){

    }
    public boolean critical_section(String d_acc_no, String cred_acc_no,
            String password, int amt) {
        int deb_ind = 0;
        int cred_ind = 0;
        try {

            Document doc = getFromDatabase(Integer.parseInt(d_acc_no));
            Document another_doc = getFromAnotherDatabase(Integer.parseInt(d_acc_no));

            // System.out.println(doc.values());
            String pass = doc.get("Password").toString();
            String another_pass = another_doc.get("Password").toString();

            System.out.println(password);

            if (pass.equals(password) && another_pass.equals(password)) {
                System.out.println("doc.get(Balance).toString()");
                System.out.println(doc.get("Balance").toString());
                int debitnewBal = (int) (Math.round((Double) doc.get("Balance")) - amt);
//                System.out.println("debitnewBal: "+debitnewBal);
                String acc_No = doc.get("AccountNo").toString();
                updateBalance((Integer.parseInt(acc_No)), debitnewBal);
//                System.out.println(doc.get("AccountNo"));
//                System.out.println(doc.get("Balance"));
                updateAnotherBalance((Integer.parseInt(acc_No)), debitnewBal);



                int credit_newBal = (int) (Math.round((Double) doc.get("Balance")) + amt);
                updateBalance(Integer.parseInt(cred_acc_no), credit_newBal);
                updateAnotherBalance(Integer.parseInt(cred_acc_no), credit_newBal);

//                updateAnotherBalance((Integer.parseInt(another_doc.get("AccountNo").toString())), debitnewBal);

                return true;
            } else {
                System.out.println("Invalid credentials");
            }

            for (int i = 0; i < a.size(); i++) {
                if (a.get(i).acc_no.equals(d_acc_no) &&
                        a.get(i).password.equals(password)) {
                    deb_ind = i;
                }
                if (a.get(i).acc_no.equals(cred_acc_no)) {
                    cred_ind = i;
                }
            }
            if (a.get(deb_ind).balance < amt)
                return false;
            else {
                a.get(deb_ind).balance -= amt;
                a.get(cred_ind).balance += amt;
                b.get(deb_ind).balance -= amt;
                b.get(cred_ind).balance += amt;
                return true;

            }
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
            System.out.println(e.getMessage() + "\nCannot access datastore 1\nTrying to access backup datastore");
            for (int i = 0; i < b.size(); i++) {
                if (b.get(i).acc_no.equals(d_acc_no) &&
                        b.get(i).password.equals(password)) {
                    deb_ind = i;
                }
                if (b.get(i).acc_no.equals(cred_acc_no)) {
                    cred_ind = i;
                }
            }
            if (b.get(deb_ind).balance < amt)
                return false;
            else {
                b.get(deb_ind).balance -= amt;
                b.get(cred_ind).balance += amt;
                return true;
            }
        }
    }

    public void receiveRequest(int i, int n) throws RemoteException {
        System.out.println("Recieved request from " + i);
        if (RN[i] <= n) {
            RN[i] = n;
            if (token.getToken()[i] + 1 == RN[i]) {
                if (token.getOwner() == serverNo) {
                    if (critical) {
                        System.out.println("Add to queue");
                        token.getQueue()[token.getTail()] = i;
                        token.setTail(token.getTail() + 1);
                    } else {
                        System.out.println("Queue empty, setting owner");
                        token.setOwner(i);
                    }
                }
            }
        }
    }

    public void releaseToken() throws RemoteException {
        token.setToken(serverNo, RN[serverNo]);
        if (token.getHead() != token.getTail()) {
            System.out.println("Release token");
            token.setOwner(token.getQueue()[token.getHead()]);
            System.out.println("New owner" + token.getOwner());
            token.setHead(token.getHead() + 1);
        }
    }

    public static void main(String[] args) {
        try {

            // Document doc = getFromDatabase(123456);
            // System.out.println(doc.values());
            System.out.println("testing: "+getFromAnotherDatabase(123456));
            Registry reg = LocateRegistry.createRegistry(8000);
            reg.rebind("bankServer0", new Server(0));
            Registry reg1 = LocateRegistry.createRegistry(8001);
            reg1.rebind("bankServer1", new Server(1));
            Registry reg2 = LocateRegistry.createRegistry(8002);
            reg2.rebind("bankServer2", new Server(2));
            System.out.println("3 servers are running now ");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class Account {
    String acc_no;
    String password;
    int balance;

    Account(String acc_no, String password, int balance) {
        this.acc_no = acc_no;
        this.password = password;
        this.balance = balance;
    }

    public int checkBalance(String acc_no, String password) {
        if (this.acc_no.equals(acc_no) && this.password.equals(password))
            return this.balance;
        else
            return -1;
    }

    public boolean checkValid(String acc_no, String password) {
        if (this.acc_no.equals(acc_no) && this.password.equals(password))
            return true;
        else
            return false;
    }
}
