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
import main.java.DatasetUploader.*;


    //This is class exists to upload data compiled by hand into a specific collection of folders and files into my database.
    //Another option would have been to designed and build a measurement parsing engine that was structure agnostic and which would
    //simply intake any measurement straight from the usual measurement software packages - but this approach allows me to include
    //taxonomical data in the directory structure and works in accordance with the system used by the main source of data I've
    //integrated into the app so far.

public class MeasurementSorter {
    
    public ArrayList<Measurement> sortMeasurements(){

        //First let's search our root directory for a folder named "measurements" and log all of the .txt files within

        System.out.println("Searching measurements directory...");

        ArrayList<Path> paths = new ArrayList<Path>();

        try(Stream<Path> stream = Files.walk(Paths.get("measurements"))){
            //Although expensive compared to .toString().endsWith(), PathMatcher doesn't risk returning NullPointerExceptions
            PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**.txt");
            stream.filter(f -> matcher.matches(f))/*.peek(System.out::println)*/.forEach(paths::add);
        } catch (Exception e) {
            System.out.println("Panic! " + e);
        }

        //Now that we have a list of valid paths let's create a list of the measurements we plan on ingesting. First we need a way to store
        //important information we can assess from filename and path - let's create an object to bundle that information

        ArrayList<Measurement> measurements = new ArrayList<Measurement>();

        System.out.println("Creating measurements from files...");
        
        for(Path path : paths){

            //We can extract the data we need about each file from its filepath
            //Format: measurements\Brand\(Model)(Variant)(Side)(Seating)(Extension)
            String pathString = path.toString();
            
            //remove "measurements\"
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

            //Now let's extract the measurement inside the file
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

            //This measurement has a standardised TXT export format so we know that for every file the first
            //14 rows are metadata, let's remove that from our processedRows ArrayList
            
            processedRows.subList(0, 14).clear();

            //Now we have 527 rows with three values, for the time being we're not interested in the phase
            //data but for data integrity we're going to keep it. Let's split apart these lists into the
            //measurements we want

            ArrayList<Double> frequencies = new ArrayList<Double>();
            ArrayList<Double> magnitudes = new ArrayList<Double>();
            ArrayList<Double> phase = new ArrayList<Double>();

            for(String[] row : processedRows){
                frequencies.add(Double.parseDouble(row[0]));
                magnitudes.add(Double.parseDouble(row[1]));
                phase.add(Double.parseDouble(row[2]));
            }

            //Now we have a set of the information we need into our base measurement class
            Measurement measurement = new Measurement();
            measurement.setLocation(pathString);
            measurement.setBrand(brand);
            measurement.setVariant(variant);
            measurement.setFullName(fullName);
            measurement.setSide(side.charAt(0));
            measurement.setSeating(Integer.parseInt(String.valueOf(seating)));
            measurement.setOriginalFrequencies(frequencies);
            measurement.setOriginalMagnitudes(magnitudes);
            measurement.setOriginalPhase(phase);
            //Please inspect the measurement to see how the resampling engine and ppr calculation algorithm function
            measurement.resample();
            measurement.calculatePpr();
            //Now we have measurement objects with everything we need
            measurements.add(measurement);
        }
        
        return measurements;
    }

    //Although we want measurements saved and accessible, we certainly don't want to pass all that information around for our listing functionality
    //Plus many measurements are of the model, even measurements of the same cup of the same model! Let's bundle these into unit samples, and then 
    //into measurements of a single model of headphone across multiple units. From there we can summarise what we know about that headphone 
    //from the data we've created. 

    public ArrayList<Sample> bundleToSamples(ArrayList<Measurement> measurements){

        System.out.println("Bundling measurements into unit samples...");

        ArrayList<String> unitsBundled = new ArrayList<String>();
        ArrayList<Sample> unitSamples = new ArrayList<Sample>(); 

        for(Measurement measurement : measurements){
            
            //First let's go through all of our measurements and identify what units make up those measurements
            //We create one variant for each individual headphone sample uncovered and mark it as accounted
            //for, such that our loop won't go back over it when it encounters another side / seat measurement
            //for that unit. 

            //Check hasn't already been bundled
            if(unitsBundled.contains(measurement.getFullName())){
                System.out.println(measurement.getFullName() + " is listed as already being packaged into a unit set - ignoring...");
            } else {
                //Gather together all the measurements created from that unit
                System.out.println("No unit sample package exists for " + measurement.getFullName() + "! Creating one now...");
                
                Measurement l1 = new Measurement();
                Measurement l2 = new Measurement();
                Measurement l3 = new Measurement();
                Measurement r1 = new Measurement();
                Measurement r2 = new Measurement();
                Measurement r3 = new Measurement();
                
                //Identify unit's L and R 1 through 3 measurements, store them
                for(int i = 0; i < measurements.size(); i++){
                    if(measurements.get(i).getFullName().equals(measurement.getFullName())){
                        if(measurements.get(i).getSide() == 'L'){
                            switch(measurements.get(i).getSeating()){
                                case 1: {
                                    l1 = measurements.get(i);
                                    break;
                                }
                                case 2: {
                                    l2 = measurements.get(i);
                                    break;
                                }
                                case 3: {
                                    l3 = measurements.get(i);
                                    break;
                                }
                            }
                        } else if(measurements.get(i).getSide() == 'R'){
                            switch(measurements.get(i).getSeating()){
                                case 1: {
                                    r1 = measurements.get(i);
                                    break;
                                }
                                case 2: {
                                    r2 = measurements.get(i);
                                    break;
                                }
                                case 3: {
                                    r3 = measurements.get(i);
                                    break;
                                }
                            }
                        }
                    }
                }

                if(l1 == null || l2 == null || l3 == null || r1 == null || r2 == null || r3 == null){
                    System.out.println("Failed to find datasets required to create unit sample package: ");
                }
                
                //Create a sample with them
                System.out.println("Packaging unit sample...");
                Sample sample = new Sample(measurement.getBrand(), measurement.getVariant(), measurement.getFullName(), l1, l2, l3, r1, r2, r3);
                System.out.println("Unit set for " + measurement.getFullName() + " created. Calculating ratings...");
                unitSamples.add(sample);
                unitsBundled.add(measurement.getFullName());
            }
        }

        return unitSamples;
    }

    public ArrayList<Model> bundleToModels(ArrayList<Sample> samples, ArrayList<Measurement> measurements){

        ArrayList<Model> models = new ArrayList<Model>();
        
        //Now let's check for situations where the same model of headphone has multiple tested units, and create
        //higher quality measurements out of that volume of data then eliminate any resulting redundancies

        System.out.println("Searching for incidences multiple samples of the same unit...");

        ArrayList<ArrayList<Sample>> joinedSamples = new ArrayList<ArrayList<Sample>>();
        ArrayList<String> multiSampleModelsFound = new ArrayList<String>();

        for(Sample sample : samples){

            String sampleTitle = sample.getFullName().substring(0, sample.getFullName().length() - 3);
            String multiSampleIdent = sample.getFullName().substring(sample.getFullName().length() - 2, sample.getFullName().length() - 1);

            //If part of a multi-unit set
            if(multiSampleIdent.equals("S")){
                
                Boolean alreadyFound = false;

                //Check to see if this unit has already been packaged with its other units
                for(String samplesFound : multiSampleModelsFound){
                    //If you find the same model in multiSampleModelsFound skip this one 
                    if(samplesFound.equals(sampleTitle)){
                        alreadyFound = true;
                    }
                }

                //If not, create an ArrayList for this model including this sample and matching samples 
                if(alreadyFound == false){
                    
                    System.out.println(sample.getFullName() + " is one sample of a multi sample set and has not yet been bundled and catalogued");
                
                    System.out.println("Finding samples of the same model...");
    
                    ArrayList<Sample> matchingSamples = new ArrayList<Sample>();
    
                    //Iterate through all samples, if you find a sample with the same model name add it to the matchingSamples list
                    for(Sample sample2: samples){
                        if(sampleTitle.equals(sample2.getFullName().substring(0, sample2.getFullName().length() - 3)))
                        matchingSamples.add(sample2);
                    }
                    
                    //Add the matching sample set to the list of multiSample models
                    joinedSamples.add(matchingSamples);
                    
                    //Mark that this set has been accounted for and can be safely ignored ongoing
                    multiSampleModelsFound.add(sampleTitle);

                    System.out.println("Multi sample model set identified and stored...");

                }

            }
            
        }

        //Now we have all of our multi sample sets identified, we need to remove the already bundled samples from our sample set
        //Let's go backwards over our list of headphone models and identify if they're part of multiple unit sample model sets
        //If they are, let's remove them from the list.
        for(int i = samples.size() - 1; i > 0; i--){
            if(multiSampleModelsFound.contains(samples.get(i).getFullName().substring(0, samples.get(i).getFullName().length() - 3))){
                samples.remove(i);
            }
        }

        //Now we can go through both our multi-sample sets and create headphone models from them. We can do the same for our single
        //sample models using different constructors for each. In the Model creation process we follow different procedures depending
        //on how many samples are provided

        //Create models from matched samples
        for(ArrayList<Sample> joinedSample : joinedSamples){
            String fullName = joinedSample.get(0).getFullName().substring(0, joinedSample.get(0).getFullName().length() - 3);
            String variant = joinedSample.get(0).getModel().substring(0, joinedSample.get(0).getModel().length() - 3);
            String brand = joinedSample.get(0).getBrand();
            if(joinedSample.size() == 2){
                Model model = new Model(brand, variant, fullName, joinedSample.get(0), joinedSample.get(1));
                models.add(model);
            } else if(joinedSample.size() == 3){
                Model model = new Model(brand, variant, fullName, joinedSample.get(0), joinedSample.get(1), joinedSample.get(2));
                models.add(model);
            }
        }

        //Create models from single samples
        for(Sample sample : samples){
            String fullName = sample.getFullName();
            String variant = sample.getModel();
            String brand = sample.getBrand();
            Model model = new Model(brand, variant, fullName, sample);
            models.add(model);
        }

        //Sort the set by Final Score so we can examine the output from console
        Collections.sort(models, Model.CompareByFinalScoreAscending);
        
        for(int i = 0; i < models.size(); i ++){
            System.out.println((models.size() - i) + " : " + models.get(i).toString());
        }

        System.out.println("Dataset ingest operation successful!\r\n" + models.size() + " headphone variants catalogued using " + measurements.size() +  " measurements.");

        for(int i = 0; i < models.size(); i ++){
            System.out.println((models.size() - i) + " : " + models.get(i).getFullName() + " -> " + models.get(i).getFinalScore());
        }

        //Return the set
        return models;
    }

}
