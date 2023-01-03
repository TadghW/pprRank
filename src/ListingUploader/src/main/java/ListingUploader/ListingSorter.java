package main.java.ListingUploader;

import java.io.BufferedReader;
import java.util.*;
import java.util.stream.Stream;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.io.FileReader;
import main.java.ListingUploader.Listing;


public class ListingSorter {
    
    public ArrayList<Listing> readListings(String path){

        ArrayList<Listing> listings = new ArrayList<Listing>();

        System.out.println("Reading listings file...");
    
        ArrayList<String> rows = new ArrayList<String>();
        //Read the file line by line
        try(BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null){
                rows.add(line);
            }
            br.close();
        } catch (Exception e) {
            System.out.println("Can't find " + path + " exception: " + e); 
        }
    
        //Rows is now a list of Strings delimited with commas and with newline characters at the end, 
        //let's break them down into arrays of values without commas or newline characters
        ArrayList<String[]> processedRows = new ArrayList<String[]>();
        
        for(String row : rows){
            String cleanRow = row.replace("\r\n","");
            String[] rowAsArray = cleanRow.split(",");
            processedRows.add(rowAsArray);
        }
        
        ArrayList<String> fullNames = new ArrayList<String>();
        ArrayList<String> pprs = new ArrayList<String>();
        ArrayList<String> price = new ArrayList<String>();
        ArrayList<String> format = new ArrayList<String>();
        ArrayList<String> type = new ArrayList<String>();
        ArrayList<String> driver = new ArrayList<String>();
        ArrayList<String> notes = new ArrayList<String>();


        //int i = 0 as first column is legend
        for(int i = 1; i < processedRows.get(0).length; i++){

                fullNames.add(processedRows.get(0)[i]);
                pprs.add(processedRows.get(1)[i]);
                price.add(processedRows.get(2)[i]);
                format.add(processedRows.get(3)[i]);
                type.add(processedRows.get(4)[i]);
                driver.add(processedRows.get(5)[i]);
                notes.add("");

        }

        for(int i = 0; i < fullNames.size(); i++){
            Listing listing = new Listing(fullNames.get(i), Integer.parseInt(pprs.get(i)), Integer.parseInt(price.get(i)), format.get(i), type.get(i), driver.get(i), notes.get(i));
            listings.add(listing);
        }

        return listings;
    }
}
