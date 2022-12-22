package main.java.DatasetUploader;

import main.java.DatasetUploader.MeasurementSorter;
import java.util.ArrayList;
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

public class DatasetUploader {

    public static void main(String[] args) {

        //This uploader is a relatively simple program because I'm the only intended user and don't need a user interface and will always
        //format my documents in the same way.

        //First we need to crawl the measurements folder, distill its datasets, turn them into the objects that we want to work with, and calculate their PPRs.
        //These steps are completed in the DatasetParser, Measurement, Sample, and Model classes

        System.out.println("Running dataset parser...");
        MeasurementSorter measurementSorter = new MeasurementSorter();
        ArrayList<Measurement> measurements = measurementSorter.sortMeasurements();
        ArrayList<Sample> samples = measurementSorter.bundleToSamples(measurements);
        ArrayList<Model> models = measurementSorter.bundleToModels(samples, measurements);

        /*System.out.println("Reading environment variables from dotenv...");
        Dotenv dotenv = Dotenv.configure()
        .directory("src/main/resources")
        .filename("environment.env")
        .load();
        String mongoDbUri = dotenv.get("MONGODB_URI");

        System.out.println("Attempting to connect to cluster...");
        MongoClient mongoClient = MongoClients.create(mongoDbUri);
        System.out.println("Connection successful!");
        
        //First we're going to upload the cut down information we want clients to pull from
        System.out.println("Finding collection..");
        MongoDatabase pprRankDatabase = mongoClient.getDatabase("pprRankList");
        MongoCollection<Document> headphones = pprRankDatabase.getCollection("modelSummaries");
        ArrayList<Document> headphoneSummaries = new ArrayList<Document>();

        System.out.println("Listing model summaries...");
        for(Model model : models){
            Document modelSummary = new Document("_id", new ObjectId());
            modelSummary.append("brand", model.getBrand())
            .append("model", model.getModel())
            .append("fullName", model.getFullName())
            .append("averagePpr", model.getAveragePpr())
            .append("cupVariation", model.getCupConsistency())
            .append("cupVariationScore", model.getCupConsistencyScore())
            .append("fitVariation", model.getFitConsistency())
            .append("fitVariationScore", model.getFitConsistencyScore())
            .append("sealVariation", model.getSealConsistency())
            .append("sealVariationScore", model.getSealConsistencyScore())
            .append("unitVariation", model.getUnitConsistency())
            .append("unitVariationScore", model.getUnitConsistency())
            .append("comfortIssues", model.getComfortIssues())
            .append("comfortIssuesDebuff", model.getComfortIssuesDebuff())
            .append("buildIssues", model.getBuildIssues())
            .append("buildIssuesDebuff", model.getBuildIssuesDebuff())
            .append("finalScore", model.getFinalScore())
            .append("notes", "")
            .append("averageDataset", model.getRepresentativeFrequencyResponse());
            headphoneSummaries.add(modelSummary);
        }

        System.out.println("Uploading model summaries...");

        //Upload summaries
        headphones.insertMany(headphoneSummaries);*/

    }
    
}