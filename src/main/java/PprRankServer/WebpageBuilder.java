package main.java.pprrankserver;
import main.java.pprrankserver.*;
import java.util.Comparator;
import java.util.Arrays;

import com.mongodb.client.MongoClient;

public class WebpageBuilder {

    public String build(){

        String response = "PprRank v.0.2.0 <- https://github.com/TadghW/pprRank <br>";

        Dataset[] datasetsSorted = PprRankServer.headphoneList;        

        Arrays.sort(datasetsSorted, new Comparator<Dataset>(){
            public int compare(Dataset d1, Dataset d2){
                if(d1.getPpr() > d2.getPpr()){
                    return -1;
                }
                if(d1.getPpr() < d2.getPpr()){
                    return 1;
                }
                    return 0;
            }
        });

        for(Dataset dataset : datasetsSorted){
            response +=  dataset.toString() + "<br>";
        }

        return response;
    }
    
}
