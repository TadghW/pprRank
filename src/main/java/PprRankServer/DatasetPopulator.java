package main.java.pprrankserver;

import static com.mongodb.client.model.Filters.eq;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class DatasetPopulator {

    private MongoClient mongoClient;

    public DatasetPopulator(){

        //The connection string required to connect to my cluster is stored in an .env file at my project root and is ignored by git for security 
        //purposes, if you want to recreate this project you'll need to create a environment.env at the root of your project folder including the
        //value pair MONGODB_URI=YourConnectionString

        //I load the environment variables with Cdimascio's dotenv-java module - you can find it here: https://github.com/cdimascio/dotenv-java
        //In a production you should store your environment variables in the host environment rather than in a .env file - but using a .env
        //is super useful for developing on a personal computer!

        Dotenv dotenv = Dotenv.load();
        String mongoDbUri = dotenv.get("MONGODB_URI");

        System.out.println("Attempting to connect to Basre Cluster...");

        MongoClient mongoClient = MongoClients.create(mongoDbUri);
        
        System.out.println("Connection successful!");
        
        this.mongoClient = mongoClient;

    }

    public String[] populate(){

        CodecRegistry pojoCodecRegistry = fromProviders(PojoCodecProvider.builder().automatic(true).build());


        String[]         

    }
    
}
