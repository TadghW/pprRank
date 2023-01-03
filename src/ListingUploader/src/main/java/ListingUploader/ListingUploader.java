package main.java.ListingUploader;

import main.java.ListingUploader.ListingSorter;
import java.util.ArrayList;
import java.util.Collections;
import java.io.BufferedReader;
import java.net.http.WebSocket.Listener;
import static com.mongodb.client.model.Filters.eq;
import io.github.cdimascio.dotenv.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class ListingUploader {

    public static void main(String[] args) {

        System.out.println("Reading Listings...");
        ListingSorter listingSorter = new ListingSorter();
        ArrayList<Listing> listings = listingSorter.readListings("OratoryMeasurements.csv");
        Collections.sort(listings, Listing.CompareByPprAscending);
        for(Listing listing : listings){
            System.out.println(listing.getFullName() + " - > " + listing.getPpr() + " for " + listing.getPrice());
        }

        System.out.println("Reading environment variables from dotenv...");
        Dotenv dotenv = Dotenv.configure()
        .directory("src/main/resources")
        .filename("environment.env")
        .load();
        String mongoDbUri = dotenv.get("MONGODB_URI");

        System.out.println("Attempting to connect to cluster...");
        MongoClient mongoClient = MongoClients.create(mongoDbUri);
        
        System.out.println("Connecting to database..");
        MongoDatabase pprRankDatabase = mongoClient.getDatabase("headphones-science");
        System.out.println("Finding pprRank collection...");
        MongoCollection<Document> modelSummaries = pprRankDatabase.getCollection("preferenceRank");
        ArrayList<Document> headphoneSummaries = new ArrayList<Document>();

        System.out.println("Listing model summaries...");
        for(Listing listing : listings){
            Document modelSummary = new Document("_id", new ObjectId());
            modelSummary.append("fullName", listing.getFullName())
            .append("ppr", listing.getPpr())
            .append("price", listing.getPrice())
            .append("format", listing.getFormat())
            .append("type", listing.getType())
            .append("driver", listing.getDriver())
            .append("notes", listing.getNotes());
            headphoneSummaries.add(modelSummary);
        }

        System.out.println("Uploading model summaries...");

        //Upload summaries
        modelSummaries.insertMany(headphoneSummaries);

        System.out.println("Upload complete.");
    }
    
}