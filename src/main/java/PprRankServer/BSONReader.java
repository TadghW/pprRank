package main.java.pprrankserver;

import org.bson.Document;
import org.bson.types.ObjectId;
import com.mongodb.BasicDBList; 
import java.util.ArrayList;
import main.java.pprrankserver.Dataset; 

public class BSONReader {

    public Dataset convertToDataset(Document document){

        ObjectId id = document.getObjectId("id");
        String name = document.getString("name");
        ArrayList<Object> magnitudes = (ArrayList<Object>) document.get("frequencyResponse");
        double ppr = document.getDouble("ppr");

        double[] magnitudesArr = new double[magnitudes.size()];
        for(int i = 0; i < magnitudes.size(); i++){
            magnitudesArr[i] = new Double(magnitudes.get(i).toString());
        }

        Dataset dataset = new Dataset(id, name, magnitudesArr, ppr); 

        return dataset;
    }
    
}
