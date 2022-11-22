package main.java.pprrankserver;

public class WebpageBuilder {

    private MongoClient mongoClient;

    WebpageBuilder(MongoClient mongoClient){
        this.mongoClient = mongoClient;
    }

    public String build(String requestSummary){
        return requestSummary;
    }
    
}
