package main.java.DatasetUploader;

import main.java.DatasetUploader.DatasetParser;
import java.util.ArrayList;
import java.io.BufferedReader;
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

public class DatasetUploader {

    public static void main(String[] args) {


        //This uploader is a relatively simple program because I'm the only intended user and don't need a user interface and will always
        //format my documents in the same way. It reads the file "headphones.csv" at the project root, which is a list of headphone frequency
        //response datasets. I can copy and paste from the software that I use to take measurements, or from software I use to resample existing
        //measurements into that file and run this program to process the data and upload reformatted datasets to the PPRList database.

        //First we need to read the file, distill its datasets, turn them into the objects that we want to work with, and calculate their PPRs.
        //These steps are completed in the DatasetParser and Dataset clases
        //DataSetParser.parse() returns a list of Dataset objects it's produced from the CSV

        System.out.println("Reading the datasets file...");
        DatasetParser datasetParser = new DatasetParser();
        ArrayList<Dataset> datasets = datasetParser.parse();

        //Now we need to connect to our database cluster
        System.out.println("Reading environment variables from dotenv...");
        Dotenv dotenv = Dotenv.configure()
        .directory("src/main/resources")
        .filename("environment.env")
        .load();
        String mongoDbUri = dotenv.get("MONGODB_URI");

        System.out.println("Attempting to connect to Basre Cluster...");
        MongoClient mongoClient = MongoClients.create(mongoDbUri);
        System.out.println("Connection successful!");
        
        //Target the relevant collections
        System.out.println("Finding collection..");
        MongoDatabase pprRankDatabase = mongoClient.getDatabase("PPR-Listing");
        MongoCollection<Document> headphones = pprRankDatabase.getCollection("Headphones");

        //Create documents from our list of datasets
        ArrayList<Document> documents = new ArrayList<Document>();
        
        for(Dataset dataset : datasets){
            Document headphoneDataset = new Document("_id", new ObjectId());
            headphoneDataset.append("name", dataset.getName())
            .append("frequencyResponse", dataset.getMagnitudes())
            .append("ppr", dataset.getPpr());
            documents.add(headphoneDataset);
        }

        //Upload documents to the database
        headphones.insertMany(documents);

    }
    
}