package main.java.pprrankserver;
import main.java.pprrankserver.*;
import java.util.Comparator;
import java.util.Arrays;

import com.mongodb.client.MongoClient;

public class ResponseBuilder {

    public String build(){

        String response = "PprRank v.0.8.2 <- https://github.com/TadghW/pprRank <br>";

        ModelSummary[] datasetsSorted = PprRankServer.headphoneList;        

        Arrays.sort(datasetsSorted, new Comparator<ModelSummary>(){
            public int compare(ModelSummary d1, ModelSummary d2){
                if(d1.getFinalScore() > d1.getFinalScore()){
                    return -1;
                }
                if(d1.getFinalScore() < d2.getFinalScore()){
                    return 1;
                }
                    return 0;
            }
        });

        for(ModelSummary modelSummary : datasetsSorted){
            response +=  modelSummary.toString() + "<br>";
        }

        return response;
    }
    
}
