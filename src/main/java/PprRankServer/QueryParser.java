package main.java.pprrankserver;

import main.java.pprrankserver.Listing;
import com.fasterxml.jackson.databind.ObjectMapper;

public class QueryParser {

    public QueryParser(){}

    public String parseQuery(String query){
        
        //First char will always be / as all api requests are made to root
        query.deleteCharAt(0);
        
        //Check for query before doing all this calculation
        if(query.contains("?")){
            //? denotes the beginning of the query
            query.deleteCharAt(0);
            String[] parameters = query.split("&");
            String sortOption;
            String minPpr;
            String maxPrice;
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
                        minPpr = fieldValue[1];
                        break;
                    case "maxPrice":
                        maxPrice = fieldValue[1];
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
            if(minPpr == null){
                minPpr = 0;
            }
            if(maxPrice == null){
                maxPrice = 99999;
            }
            if(format.size() == 0){
                format.add("CA");
                format.add("SA");
                format.add("Wireless CA");
                format.add("Wireless SA");
                format.add("IEM");
                format.add("Wireless IEM");
                format.add("Earspeaker");
            }
            if(cup.size() == 0){
                cup.add("open");
                cup.add("closed");
            }
            if(driver.size() == 0){
                driver.add("dynamic");
                driver.add("planar");
                driver.add("electrostatic");
                driver.add("multiple");
            }

            ArrayList<Listing> relevantListings = new ArrayList<Listing>();

            for(Listing listing : PprRankServer.headphoneList){
                
                Boolean validListing = true;
                
                //Check parameters
                if(listing.getPpr() < minPpr){
                    validListing = false;
                }
                if(listing.getPrice() > maxPrice){
                    validListing = false;
                }
                if(!format.contains(listing.getFormat())){
                    validListing = false;
                }
                if(!cup.contains(listing.getType())){
                    validListing = false;
                }
                if(!driver.contains(listing.getDriver())){
                    validListing = false;
                }

                if(validListing == true){
                    relevantListings.add(listing);
                }

            }            

            switch(sortOption){
                case "pprAsc" :
                    Collections.sort(relevantListings, Listing.sortByPprAscending);
                    break;
                case "pprDesc" :
                    Collections.sort(relevantListings, Listing.sortByPprDescending);
                    break;
                case "priceAsc" :
                    Collections.sort(relevantListings, Listing.sortByPriceAscending);
                    break;
                case "priceDesc" :
                    Collections.sort(relevantListings, Listing.sortByPriceDescending);
                    break;
                case "pprVsPriceAsc" :
                    Collections.sort(relevantListings, Listing.sortByPprVsPriceAscending);
                    break;
                case "pprVsPriceDesc" :
                    Collections.sort(relevantListings, Listing.sortByPprVsPriceDescending);
                    break;
            }
            
            StringBuilder responseBody = new StringBuilder();

            responseBody.append("[");

            ObjectMapper mapper = new ObjectMapper();

            for(Listing listing : relevantListings){
                responseBody.append(mapper.writeValueAsString(listing));
                responseBody.append(',');
            }

            responseBody.delete((responseBody.length() -1), responseBody.length());
            responseBody.append("]");

            String response = responseBody.toString();

            return response;

        }

        //Default response is to spit out the list as normal
        StringBuilder responseBody = new StringBuilder();

        responseBody.append("[");

        ObjectMapper mapper = new ObjectMapper();

        for(Listing listing : PprRankServer.headphoneList){
            responseBody.append(mapper.writeValueAsString(listing));
            responseBody.append(',');
        }



    }
    
}
