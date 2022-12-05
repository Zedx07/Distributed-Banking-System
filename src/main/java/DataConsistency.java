//import com.mongodb.client.MongoClient;
//import com.mongodb.client.MongoClients;
//import com.mongodb.client.MongoCollection;
//import com.mongodb.client.MongoDatabase;
//import org.bson.Document;
//
//import java.util.HashMap;
//
//public class DataConsistency {
//    public static void main(String[] args) {
//        MongoClient client = MongoClients.create("mongodb+srv://Shubham:1234@cluster0.srmnd6r.mongodb.net/?retryWrites=true&w=majority");
//
//        MongoDatabase db = client.getDatabase("BD");
//
//        MongoCollection coll = db.getCollection("coll_BD");
//
//        Document sample = new Document("id", "1").append("name","Shubham");
//
//        coll.insertOne(sample);
//
//    }
//}
