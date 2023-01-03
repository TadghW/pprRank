package main.java.pprrankserver;

import java.util.ArrayList;
import org.bson.Document;
import java.util.Comparator;

public class Listing {

    private String fullName;
    private int ppr;
    private int price;
    private String format;
    private String type;
    private String driver;
    private String notes;

    public Listing(Document document){

        System.out.println("Converting document to dataset...");
        
        this.fullName = String.valueOf(document.get("fullName"));
        this.ppr = (int) Double.parseDouble(String.valueOf(document.get("ppr")));
        this.price = Integer.parseInt(String.valueOf(document.get("price")));
        this.format = String.valueOf(document.get("format"));
        this.type = String.valueOf(document.get("type"));
        this.driver = String.valueOf(document.get("driver"));
        this.notes = String.valueOf(document.get("notes"));

        System.out.println("Converted!");

    }

    public String getFullName() {
        return fullName;
    }

    public int getPpr() {
        return ppr;
    }

    public int getPrice() {
        return price;
    }

    public String getFormat() {
        return format;
    }

    public String getType() {
        return type;
    }

    public String getDriver() {
        return driver;
    }

    public String getNotes() {
        return notes;
    }

    public static Comparator<Listing> CompareByPprAscending = new Comparator<Listing>() {
        public int compare(Listing l1, Listing l2){
            double l1ppr = l1.getPpr();
            double l2ppr = l2.getPpr();
            if(l1ppr > l2ppr){
                return 1;
            } else if (l1ppr < l2ppr){
                return -1;
            } else {
                return 0;
            }
        }
    };

    
}
