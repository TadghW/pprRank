package main.java.pprrankserver;

import java.util.ArrayList;
import org.bson.Document;

public class ModelSummary {

    private String brand;
    private String model;
    private String fullName;
    private Double averagePpr;
    private Double cupConsistency;
    private int cupConsistencyScore;
    private Double fitConsistency;
    private int fitConsistencyScore;
    private Double sealConsistency;
    private int sealConsistencyScore;
    private Double unitConsistency;
    private int unitConsistencyScore;
    private String comfortIssues = "false";
    private int comfortIssuesDebuff = 0;
    private String buildIssues = "false";
    private int buildIssuesDebuff  = 0;
    private Double finalScore;
    private String confidence;
    private String notes;

    public ModelSummary(Document document){

        System.out.println("Converting document to dataset...");

        this.brand = String.valueOf(document.get("brand"));
        this.model = String.valueOf(document.get("model"));
        this.fullName = String.valueOf(document.get("fullName"));
        this.averagePpr = Double.parseDouble(String.valueOf(document.get("averagePpr")));
        this.cupConsistency = Double.parseDouble(String.valueOf(document.get("cupVariation")));
        this.cupConsistencyScore = Integer.parseInt(String.valueOf(document.get("cupVariationScore")));
        this.fitConsistency = Double.parseDouble(String.valueOf(document.get("fitVariation")));
        this.fitConsistencyScore = Integer.parseInt(String.valueOf(document.get("fitVariationScore")));
        this.sealConsistency = Double.parseDouble(String.valueOf(document.get("sealVariation")));
        this.sealConsistencyScore = Integer.parseInt(String.valueOf(document.get("sealVariationScore")));
        //this.unitConsistency = Double.parseDouble(String.valueOf(document.get("unitVariation")));
        //this.unitConsistencyScore = Integer.parseInt(String.valueOf(document.get("unitVariationScore")));
        this.comfortIssues = String.valueOf(document.get("comfortIssues"));
        this.comfortIssuesDebuff = Integer.parseInt(String.valueOf(document.get("comfortIssuesDebuff")));
        this.buildIssues = String.valueOf(document.get("buildIssues"));
        this.buildIssuesDebuff = Integer.parseInt(String.valueOf(document.get("buildIssuesDebuff")));
        this.finalScore = Double.parseDouble(String.valueOf(document.get("finalScore")));
        this.confidence = String.valueOf(document.get("confidence"));
        this.notes = String.valueOf(document.get("notes"));

        System.out.println("Converted!");

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

    public Double getAveragePpr() {
        return averagePpr;
    }

    public Double getCupConsistency() {
        return cupConsistency;
    }

    public int getCupConsistencyScore() {
        return cupConsistencyScore;
    }

    public Double getFitConsistency() {
        return fitConsistency;
    }

    public int getFitConsistencyScore() {
        return fitConsistencyScore;
    }

    public Double getSealConsistency() {
        return sealConsistency;
    }

    public int getSealConsistencyScore() {
        return sealConsistencyScore;
    }

    public Double getUnitConsistency() {
        return unitConsistency;
    }

    public int getUnitConsistencyScore() {
        return unitConsistencyScore;
    }

    public String getComfortIssues() {
        return comfortIssues;
    }

    public int getComfortIssuesDebuff() {
        return comfortIssuesDebuff;
    }

    public String getBuildIssues() {
        return buildIssues;
    }

    public int getBuildIssuesDebuff() {
        return buildIssuesDebuff;
    }

    public Double getFinalScore() {
        return finalScore;
    }

    public String getConfidence() {
        return confidence;
    }

    public String getNotes() {
        return notes;
    }
    
}
