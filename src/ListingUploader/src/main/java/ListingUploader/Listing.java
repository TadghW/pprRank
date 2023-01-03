package main.java.ListingUploader;

import java.util.ArrayList;
import java.util.Comparator;

public class Listing {

    private String brand;
    private String model;
    private String fullName;
    private double ppr;
    private int price;
    private String format;
    private String type;
    private String driver;
    private String notes;

    public Listing(String fullName, int ppr, int price, String format, String type, String driver, String notes){
        this.fullName = fullName;
        this.ppr = ppr;
        this.price = price;
        this.format = format;
        this.type = type;
        this.driver = driver;
        this.notes = notes;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public String getFullName() {
        return fullName;
    }

    public double getPpr() {
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
            Double l1ppr = l1.getPpr();
            Double l2ppr = l2.getPpr();
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