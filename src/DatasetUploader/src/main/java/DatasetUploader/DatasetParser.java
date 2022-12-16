package main.java.DatasetUploader;

import java.io.BufferedReader;
import java.util.*;
import java.util.stream.Stream;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.io.FileReader;
import main.java.DatasetUploader.Dataset;
import main.java.DatasetUploader.Variant;

public class DatasetParser {
    
        //This is class exists to upload data compiled by hand into a specific collection of folders and files into my database.
        //Another option would have been to designed and build a dataset parsing engine that was structure agnostic and which would
        //simply intake any dataset straight from the usual measurement software packages - but this approach allows me to include
        //taxonomical data in the directory structure and works in accordance with the system used by the main source of data I've
        //integrated into the app so far.

    public void parse(){

        //First let's search our root directory for a folder named "datasets" and log all of the .txt files within

        System.out.println("Searching datasets directory...");

        ArrayList<Path> paths = new ArrayList<Path>();

        try(Stream<Path> stream = Files.walk(Paths.get("datasets"))){
            //System.out.println("Datasets discovered:");
            //Although expensive compared to .toString().endsWith(), PathMatcher doesn't risk returning NullPointerExceptions
            PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**.txt");
            stream.filter(f -> matcher.matches(f))/*.peek(System.out::println)*/.forEach(paths::add);
        } catch (Exception e) {
            System.out.println("Panic! " + e);
        }

        //Now that we have a list of valid paths let's create a list of the datasets we plan on ingesting. First we need a way to store
        //important information we can assess from filename and path - let's create an object to bundle that information

        ArrayList<Dataset> datasets = new ArrayList<Dataset>();

        System.out.println("Creating datasets from files...");
        
        for(Path path : paths){

            //We can extract the data we need about each file from its filepath
            //Format: datasets\Brand\(Model)(Variant)(Side)(Seating)(Extension)
            String pathString = path.toString();
            
            //remove "datasets\"
            String trimmedPathString = pathString.substring(pathString.indexOf("\\") + 1);
            
            //extract brand info
            String brand = trimmedPathString.substring(0, trimmedPathString.indexOf("\\"));    
            
            //due to filename standardisation we know that everything between the brand and final 6 characters (L1.txt etc) are the
            //headphone model and variant  
            String variant = trimmedPathString.substring(trimmedPathString.indexOf("\\") + 1, trimmedPathString.length() - 7);

            String fullName = brand + " " + variant;

            //which means that the first char of that set is the side
            String side = trimmedPathString.substring(trimmedPathString.length() - 6, trimmedPathString.length() - 5);

            //the second is the seating
            String seating = trimmedPathString.substring(trimmedPathString.length() - 5, trimmedPathString.length() - 4);

            //and the remaining 4 can be ignored, as we know that they must be ".txt"

            //Now let's extract the dataset inside the file
            ArrayList<String> rows = new ArrayList<String>();

            //Read the file line by line
            try(BufferedReader br = new BufferedReader(new FileReader(pathString))) {
                String line;
                while ((line = br.readLine()) != null){
                    rows.add(line);
                }
                br.close();
            } catch (Exception e) {
                System.out.println("Can't find " + pathString + " exception: " + e); 
            }
       
            //Rows is now a list of Strings delimited with commas and with newline characters at the end, 
            //let's break them down into arrays of values without commas or newline characters

            ArrayList<String[]> processedRows = new ArrayList<String[]>();
            
            for(String row : rows){
                String cleanRow = row.replace("\r\n","");
                String[] rowAsArray = cleanRow.split(",");
                processedRows.add(rowAsArray);
            }

            //This dataset has a standardised TXT export format so we know that for every file the first
            //14 rows are metadata, let's remove that from our processedRows ArrayList
            
            processedRows.subList(0, 14).clear();

            //Now we have 527 rows with three values, for the time being we're not interested in the phase
            //data but for data integrity we're going to keep it. Let's split apart these lists into the
            //datasets we want

            ArrayList<Double> frequencies = new ArrayList<Double>();
            ArrayList<Double> magnitudes = new ArrayList<Double>();
            ArrayList<Double> phase = new ArrayList<Double>();

            for(String[] row : processedRows){
                frequencies.add(Double.parseDouble(row[0]));
                magnitudes.add(Double.parseDouble(row[1]));
                phase.add(Double.parseDouble(row[2]));
            }

            //Now we have a set of the information we need into our base dataset class
            Dataset dataset = new Dataset();
            dataset.setLocation(pathString);
            dataset.setBrand(brand);
            dataset.setVariant(variant);
            dataset.setFullName(fullName);
            dataset.setSide(side.charAt(0));
            dataset.setSeating(Integer.parseInt(String.valueOf(seating)));
            dataset.setOriginalFrequencies(frequencies);
            dataset.setOriginalMagnitudes(magnitudes);
            dataset.setOriginalPhase(phase);
            dataset.resample();
            dataset.calculatePpr();
            datasets.add(dataset);
        }
        
        System.out.println("Bundling into variants...");

        ArrayList<String> variantsCovered = new ArrayList<String>();
        ArrayList<Variant> variantPackages = new ArrayList<Variant>(); 
        
        //Assuming that our reasmpling engine and ppr calculation are functioning as we expect them to (remains to be verified)
        //what we actually want to do here is to sort each dataset into packs sorted by headphone variant.

        for(Dataset dataset : datasets){
            
            //If the variants covered list marks this variant as complete ignore
            if(variantsCovered.contains(dataset.getFullName())){
                System.out.println(dataset.getFullName() + " already has a variant package in the list - ignoring...");
            } else {
                
                //Otherwise make a new variant

                System.out.println("No variant package exists for " + dataset.getFullName() + "! Creating one now...");
                Dataset l1 = new Dataset();
                Dataset l2 = new Dataset();
                Dataset l3 = new Dataset();
                Dataset r1 = new Dataset();
                Dataset r2 = new Dataset();
                Dataset r3 = new Dataset();
                
                //Find the datasets required to gather L1-3 and R1-3 into Variant
                for(int i = 0; i < datasets.size(); i++){

                    if(datasets.get(i).getFullName().equals(dataset.getFullName())){
                        if(datasets.get(i).getSide() == 'L'){
                            switch(datasets.get(i).getSeating()){
                                case 1: {
                                    System.out.println("Found " + datasets.get(i).getFullName() + " " + datasets.get(i).getSide() + datasets.get(i).getSeating());
                                    l1 = datasets.get(i);
                                    break;
                                }
                                case 2: {
                                    System.out.println("Found " + datasets.get(i).getFullName() + " " + datasets.get(i).getSide() + datasets.get(i).getSeating());
                                    l2 = datasets.get(i);
                                    break;
                                }
                                case 3: {
                                    System.out.println("Found " + datasets.get(i).getFullName() + " " + datasets.get(i).getSide() + datasets.get(i).getSeating());
                                    l3 = datasets.get(i);
                                    break;
                                }
                            }
                        } else if(datasets.get(i).getSide() == 'R'){
                            switch(datasets.get(i).getSeating()){
                                case 1: {
                                    System.out.println("Found " + datasets.get(i).getFullName() + " " + datasets.get(i).getSide() + datasets.get(i).getSeating());
                                    r1 = datasets.get(i);
                                    break;
                                }
                                case 2: {
                                    System.out.println("Found " + datasets.get(i).getFullName() + " " + datasets.get(i).getSide() + datasets.get(i).getSeating());
                                    r2 = datasets.get(i);
                                    break;
                                }
                                case 3: {
                                    System.out.println("Found " + datasets.get(i).getFullName() + " " + datasets.get(i).getSide() + datasets.get(i).getSeating());
                                    r3 = datasets.get(i);
                                    break;
                                }
                            }
                        }
                    }
                }
                
                System.out.println("Packaging variant...");
                Variant variant = new Variant(dataset.getBrand(), dataset.getVariant(), dataset.getFullName(), l1, l2, l3, r1, r2, r3);
                System.out.println("Variant for " + dataset.getFullName() + " created. Calculating ratings...");
                variant.rateVariant();
                variantPackages.add(variant);
                variantsCovered.add(dataset.getFullName());
            }
        }

        Collections.sort(variantPackages, Variant.CompareByPprAscending);
        
        for(Variant variant : variantPackages){
            System.out.println(variant.toString());
        }

        System.out.println("Dataset ingest operation successful!\r\n" + variantPackages.size() + " headphone variants catalogued using " + datasets.size() +  " datasets.");
      

        

    
        /*
        //-----------------------------------PROCESS DATA--------------------------------

        //We have our datasets stored but lists of lists are inconvenient and syntatically unclear to work with, so let's turn them into Dataset
        //objects as defined in our Dataset class. Before we create our dataset object we also want to do a little processing to make sure that
        //we're just inserting the data we actually want

        //Measurements of transducer performance are made by playing a signal through that transducer and measuring what comes out with a microphone. Typically
        //that output is presented as absolute absolute amplitude (how loud each sample the mic when that sample played). We're analysing the pattern of the
        //difference between loudness samples, so we want our data formatted as relative loudness compared to a fixed point. By convention that fixed point is
        //500Hz, so the 500Hz sample in any dataset we store should be moved to be 0dBSPL, and every other magnitude measurement should be a measure of how much
        //louder or quieter a sample is compared to that point. In order to do that we have to take the magnitude measurement of the 500Hz sample and subtract it
        //from every sample in the set, including the 500Hz sample. 

        ArrayList<Dataset> datasetObjects = new ArrayList<Dataset>();

        for(ArrayList<String> dataset : datasets){

            //Name is always the first value in the list
            String name = dataset.get(0);

            //Now let's sort all of the following values into a list of magnitudes
            ArrayList<Double> magnitudes = new ArrayList<Double>();

            //Let's subtract our unwanted gain in the process
            //Sample 56 is always 500Hz in our process, so we want the 56th String in our list (0 is the measurement title, so we want value 56 not value 55)
            Double MagnitudeOf500Hz = Double.parseDouble(dataset.get(56));

            for(int i = 1; i < dataset.size(); i++) {
                //We'll need to parse the existing values, which are Strings, as Doubles
                Double magnitudeWithGain = Double.parseDouble(dataset.get(i));
                //Now let's remove the unwanted gain
                Double relativeMagnitude = magnitudeWithGain - MagnitudeOf500Hz;
                //And round the result to stop us uploading silly values with floating point error to the db
                Double roundedRelativeMagnitude = Math.round(relativeMagnitude * 100.0) / 100.0;
                magnitudes.add(roundedRelativeMagnitude);
            }

            //With that done we can create a Dataset object and populate it with the data we have so far (name and relative magnitude)
            Dataset datasets.get(i) = new Dataset(name, magnitudes);
            datasetObjects.add(datasets.get(i));
        }

        //-----------------------------------CALCULATING PPR-------------------------------
        
        //There's a lot of information to cover in explaining how we calculate PPR, so I've made that a separate function which can be called via the Dataset
        //object. As datasets can exist without a ppr but can't exist without their relative magnitudes, this made sense to me. Please check the Dataset class
        //for details on what's going on here.
            
        for(Dataset dataset : datasetObjects){
            dataset.calculatePpr();
        }

        System.out.println("Datasets are as follows: ");
        
        for(Dataset dataset : datasetObjects){
            System.out.println(dataset.toString());
        }

        //All done! Our datasets have been turned from CSV of absolute magnitudes into all the data we need and can now be uploaded.
        return datasetObjects;
        */
    }
}
