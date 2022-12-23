package main.java.pprrankserver;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static com.mongodb.client.model.Filters.eq;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.*;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.ConnectionString;
import java.util.ArrayList;
import main.java.pprrankserver.ModelSummary;
import java.lang.System.*;

public class DatasetPopulator {

    private MongoClient mongoClient;

    public DatasetPopulator(){

        System.out.println("pprRank v0.8.9 <- 23/12/22");

        //---------------------------------------ONLY USED WHEN TESTING IN DEVELOPMENT--------------------------------

        /* //The connection string required to connect to my database is stored in an .env file at my project root and is ignored by git for security 
        //purposes. If you want to recreate this project you'll need to create a environment.env at the root of your project folder including the
        //value pair MONGODB_URI= and whatever your connection string is

        //I load the environment variables with Cdimascio's dotenv-java module - you can find it here: https://github.com/cdimascio/dotenv-java

        //In a production you should store your environment variables in the host environment rather than in a .env file!

        //Dotenv dotenv = Dotenv.configure()
        //.directory("src/main/resources")
        //.filename("environment.env")
        //.load();

        //String connectionString = dotenv.get("MONGODB_URI");*/

        //-----------------------------------------USED FOR DEPLOYMENT IN CONTAINER------------------------------------

        //When I build my container image with docker I pass it my .env file and the MONGODB_URI variable as parameters to be set up as container-wide
        //environment variables. Doing it this way means I never have to hardcode the value in a way that exposes our DB Secrets.

        //Example: docker build --secret id=environment,src=pprRank/environment.env --tag pprrank-latest:tag pprRank

        String connectionString = System.getenv("MONGODB_URI"); 

        //It's noteworthy that mongodb uses a client settings builder for when you're attempting a connection with a greater number of parameters,
        //something I experimented with in particular when using Mongo's Codec / Codec Registry system for converting documents to Dataset classes
        //and visa-versa. I found it cumbersome and pointless considering we're only concerned with converting documents to datasets and bakc again
        //though so have implemented my own and gone back to signing in with the MongoDbURI in a string.
        
        try {
        MongoClient mongoClient = MongoClients.create(connectionString);
        System.out.println("Attempting to connect to cluster...");
        this.mongoClient = mongoClient;
        System.out.println("Connection successful!");
        } catch (Exception e) {
            System.out.println("Failed to connect to database cluster with DatasetPopulator clientSettings. Exception: " + e);
        }

    }

    public ModelSummary[] populate(){

        ArrayList<ModelSummary> datasets = new ArrayList<ModelSummary>();

        System.out.println("Accessing database...");

        MongoDatabase database = this.mongoClient.getDatabase("headphones-science");

        System.out.println("Accessing collection...");
        
        MongoCollection<Document> headphones = database.getCollection("pprRank");

        System.out.println("Retrieving datasets...");
        
        FindIterable results = headphones.find();

        System.out.println("Converting datasets...");

        try(MongoCursor<Document> cursor = results.iterator()){
            while(cursor.hasNext()) {
                datasets.add(new ModelSummary(cursor.next()));
            }
        } catch (Exception e) {
            System.out.println("DatasetPopulator.populate() can't iterate over the returned dataset collection. Exception: " + e);
        }

        System.out.println("Adding datasets to the global resource...");

        ModelSummary[] datasetArr = new ModelSummary[datasets.size()];

        for(int i = 0; i < datasets.size(); i++){
            datasetArr[i] = datasets.get(i);
        }

        System.out.println("Publishing global resource...");

        return datasetArr;
    }
    
}
