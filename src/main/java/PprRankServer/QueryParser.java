package main.java.pprrankserver;

import java.util.ArrayList;
import java.util.Collections;
import main.java.pprrankserver.Listing;
import com.fasterxml.jackson.databind.ObjectMapper;

public class QueryParser {

    public QueryParser(){}

    public String parseQuery(String url){
        
        System.out.println("QueryParser has been handed the URL" + url);

        //First char will always be / as all api requests are made to root
        String query = url.replace("/", "");
        
        //? denotes the beginning of the query
        if(query.contains("?")){
            String cleanQuery = query.replace("?", "");
            ArrayList<String> parameters = new ArrayList<String>();
            if(cleanQuery.contains("&")){
               Collections.addAll(parameters, cleanQuery.split("&"));
            } else {
                parameters.add(cleanQuery);
            }
            
            System.out.println("Parameters identified from URL = " + parameters.toString());

            String sortOption = "pprAsc";
            int minPpr = -999;
            int maxPrice = 999999;
            ArrayList<String> format = new ArrayList<String>();
            ArrayList<String> cup = new ArrayList<String>();
            ArrayList<String> driver = new ArrayList<String>();

            for(String parameter : parameters){
                
                String[] fieldValue = parameter.split("=");

                switch(fieldValue[0]){
                    case "sortOption":
                        sortOption = fieldValue[1];
                        break;
                    case "minPpr":
                        minPpr = Integer.parseInt(String.valueOf(fieldValue[1]));
                        break;
                    case "maxPrice":
                        maxPrice = Integer.parseInt(String.valueOf(fieldValue[1]));
                        break;
                    case "format":
                        format.add(fieldValue[1]);
                        break;
                    case "cup":
                        cup.add(fieldValue[1]);
                        break;
                    case "driver":
                        driver.add(fieldValue[1]);
                        break;
                }
            
            }
            if(format.size() == 0 || format.contains("All")){
                format.add("CA");
                format.add("SA");
                format.add("Wireless CA");
                format.add("Wireless SA");
                format.add("Earspeaker");
            }
            if(cup.size() == 0 || cup.contains("All")){
                cup.add("Open");
                cup.add("Closed");
            }
            if(driver.size() == 0 || driver.contains("All")){
                driver.add("Dynamic");
                driver.add("Planar");
                driver.add("Electrostatic");
                driver.add("Multiple");
            }

            ArrayList<Listing> relevantListings = new ArrayList<Listing>();
            
            System.out.println("Searching for listings with minPpr: " + minPpr + " maxPrice: " + maxPrice + " format: " + format + " cup: " + cup + " driver: " + driver);

            for(Listing listing : PprRankServer.headphoneList){
                
                Boolean validListing = true;
                
                //Check parameters
                if(listing.getPpr() < minPpr){
                    System.out.println(listing.getPpr() + "is less than " + minPpr);
                    validListing = false;
                }
                if(listing.getPrice() > maxPrice){
                    System.out.println(listing.getPrice() + " is greater than " + maxPrice);
                    validListing = false;
                }
                if(!format.contains(listing.getFormat())){
                    System.out.println(format + " does not contain " + listing.getFormat());
                    validListing = false;
                }
                if(!cup.contains(listing.getType())){
                    System.out.println(cup + " does not contain " + listing.getType());
                    validListing = false;
                }
                if(!driver.contains(listing.getDriver())){
                    System.out.println(driver + " does not contain " + listing.getDriver());
                    validListing = false;
                }

                if(validListing == true){
                    relevantListings.add(listing);
                }

            }            

            switch(sortOption){
                case "pprAsc" :
                    Collections.sort(relevantListings, Listing.CompareByPprAscending);
                    break;
                case "pprDesc" :
                    Collections.sort(relevantListings, Listing.CompareByPprDescending);
                    break;
                case "priceAsc" :
                    Collections.sort(relevantListings, Listing.CompareByPriceAscending);
                    break;
                case "priceDesc" :
                    Collections.sort(relevantListings, Listing.CompareByPriceDescending);
                    break;
                case "pprVsPriceAsc" :
                    Collections.sort(relevantListings, Listing.CompareByPprPerEuroAscending);
                    break;
                case "pprVsPriceDesc" :
                    Collections.sort(relevantListings, Listing.CompareByPprPerEuroDescending);
                    break;
            }
            
            StringBuilder responseBody = new StringBuilder();

            responseBody.append("[");

            try { 
                ObjectMapper mapper = new ObjectMapper();
                for(Listing listing : relevantListings){
                    responseBody.append(mapper.writeValueAsString(listing));
                    responseBody.append(',');
                }
            } catch (Exception e){
                System.out.println("Query Parser failed to return valid results. Exception: " + e); 
            }

            responseBody.delete((responseBody.length() -1), responseBody.length());
            responseBody.append("]");

            String response = responseBody.toString();

            return response;

        }

        //Default response is to spit out the list as normal
        StringBuilder responseBody = new StringBuilder();

        responseBody.append("[");

        try{
            ObjectMapper mapper = new ObjectMapper();
            for(Listing listing : PprRankServer.headphoneList){
                responseBody.append(mapper.writeValueAsString(listing));
                responseBody.append(',');
            }
        } catch (Exception e) {
            System.out.println("Query Parser failed to return valid results. Exception: " + e); 
        }

        responseBody.delete((responseBody.length() -1), responseBody.length());
        responseBody.append("]");

        String response = responseBody.toString();

        return response;

    }
    
}
