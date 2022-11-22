package main.java.DatasetUploader;

import java.io.BufferedReader;
import java.util.ArrayList;
import org.apache.commons.math4.stat;
import main.java.Dataset;

public class DatasetParser {
    
        //This is class exists to upload data compiled by hand onto spreadsheets into my database. For another application I once 
        //designed a complex parsing engine that would accept datasets generated straight from all common measurement software but
        //it was a total pain and for an application designed for public interaction. As I'm the only contributor to this database,
        //I'll use a standardised spreadsheet format to upload the data and use that consistency to make a simple ingest method
        //instead

    public ArrayList<Dataset> parse(){

        //------------------------------------READ CSV-------------------------------

        ArrayList<String> rows = new ArrayList<String>();

        //Read the file line by line
        try(BufferedReader br = new BufferedReader(new FileReader(headphones.csv))) {
            String line;
            while ((line = br.ReadLine()) != null){
                //Send each line to ArrayList<String> rows
                rows.add(line);
            }
        }

        //-----------------------------------CLEAN CSV---------------------------------

        //Each row in rows still has its newline characters and we want those rows as lists of values to sort them into their columns

        ArrayList<List> processedRows = new ArrayList<List>();

        for(String row : rows){
            String cleanRow = row.replace("a","");
            String[] splitRow = cleanRow.split("\r\n");
            processedRows.add(processedRows.asList(splitRow));
        }

        //By convention this data will have one title per column. We can check the number of values in the first row and subtract one 
        //(to account for our reference frequency column) to figure out how many datasets are contained within the file

        ArrayList firstRow = processedRows.get(0);
        int columnNumber = firstRow.size() - 1;

        //------------------------SORT DATA INTO DATASETS BY COLUMN-------------------

        //Now we know that we can iterate through every row and take out every datapoint for a given column (dataset) in sequence 
        //creating a list for each dataset with its values stored in order

        ArrayList<List> datasets = new ArrayList<List>();

        for(int i; i < columnNumber; i++){
            
            ArrayList<String> dataset = new ArrayList<String>();
            
            for(int j; j < 121; j++){
                ArrayList row = processedRows.get(j);
                String value = row.get(i);
                dataset.add(value);
            }

            datasets.add(dataset);   
        }

        //-----------------------------------PROCESS DATA--------------------------------

        //Measurements of transducer performance are made by playing a signal through that transducer and measuring what comes out with a microphone. Typically
        //that output is presented as absolute absolute amplitude (how loud each sample the mic when that sample played). We're analysing the pattern of the
        //difference between loudness samples, so we want our data formatted as relative loudness compared to a fixed point. By convention that fixed point is
        //500Hz, so the 500Hz sample in any dataset we store should be moved to be 0dBSPL, and every other magnitude measurement should be a measure of how much
        //louder or quieter a sample is compared to that point. In order to do that we have to take the magnitude measurement of the 500Hz sample and subtract it
        //from every sample in the set, including the 500Hz sample. 

        //Also, storing our data in Lists of Strings isn't how we actually want to imagine them or conducive to what we want to do with them, so let's turn this
        //into a list of Dataset objects

        ArrayList<Dataset> datasetObjects = new ArrayList<Dataset>();

        for(ArrayList<String> dataset : datasets){

            //Name is always the first value in the list
            String name = dataset.get(0);

            //Now let's sort all of the following values into a list of magnitudes
            ArrayList<Double> magnitudes;

            //Let's subtract our unwanted gain in the process
            //Sample 56 is always 500Hz in our process, so we want the 56th String in our list (0 is the measurement title, so we want value 56 not value 55)
            Double MagnitudeOf500Hz = Double.parseDouble(dataset.get(56));

            for(int i = 1; i < dataset.size(); i++) {
                //We'll need to parse the existing values as Doubles
                Double magnitudeWithGain = Double.parseDouble(dataset.get(i));
                //Now let's remove the gain before committing it to our new list
                Double relativeMagnitude = magnitudeWithGain - MagnitudeOf500Hz;
                magnitudes.add(relativeMagnitude);
            }

            //With that done we can create a Dataset object and populate it with the data we have so far (name and relative magnitude)
            Dataset dataset2 = new Dataset(name, magnitudes);
        }

        //-----------------------------------CALCULATING PPR-------------------------------
        
        //There's a lot of information to cover in explaining how we calculate PPR, so I've made that a separate function which can be called via the Dataset
        //object. As datasets can exist without a ppr but can't exist without their relative magnitudes, this made sense to me. Please check the Dataset class
        //for details on what's going on here.
            
        for(Dataset dataset : datasetObjects){
            dataset.calculatePpr();
        }

        //All done! Our datasets have been turned from CSV of absolute magnitudes into all the data we need and can now be uploaded.
        return datasetObjects;

    }
}
