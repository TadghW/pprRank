package main.java.DatasetUploader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import main.java.DatasetUploader.Measurement;
import main.java.DatasetUploader.Sample;

public class Model {

    private String brand;
    private String model;
    private String fullName;
    private Double averagePpr;
    private Double idealPpr;
    private Double pprDiff;
    private int price;
    private String format;
    private String cupType;
    private String driverType;
    private String notes;
    private ArrayList<Sample> samples = new ArrayList<Sample>();
    private ArrayList<Double> representativeFrequencyResponse = new ArrayList<Double>();

    public Model(String brand, String model, String fullName, Sample s1){

        this.brand = brand;
        this.model = model;
        this.fullName = fullName;

        samples.add(s1);

        ArrayList<Measurement> modelMeasurements = this.getMeasurements();  

        //Approximate the PPR of that representative dataset
        this.averagePpr = Math.round(((modelMeasurements.get(2).getPpr() + modelMeasurements.get(3).getPpr()) / 2) * 100.0) /100.0;

        this.idealPpr = s1.getIdealPpr();
        this.pprDiff = Math.round((idealPpr - averagePpr) * 10.0) / 10.0;
    }


    public Model(String brand, String model, String fullName, Sample s1, Sample s2){

        //Set model details
        this.brand = brand;
        this.model = model;
        this.fullName = fullName;

        //Add samples
        this.samples.add(s1);
        this.samples.add(s2);

        //Produces an ArrayList of every measurement from each sample in Model's samples
        ArrayList<Measurement> modelMeasurements = getMeasurements();

        //Sort them by PPR to extract median-scoring measurements
        Collections.sort(modelMeasurements, Measurement.CompareByPprAscending);

        //Create a representative dataset for the model from samples
        for(int i = 0; i < modelMeasurements.get(0).getResampledMagnitudes().length; i++){
            //Representative dataset is the mean average between the two median ppr datasets
            double sampleMagnitude = (modelMeasurements.get(5).getResampledMagnitudes()[i] + modelMeasurements.get(6).getResampledMagnitudes()[i]) / 2;
            this.representativeFrequencyResponse.add(sampleMagnitude);
        }

        //Approximate the PPR of that representative dataset
        this.averagePpr = Math.round(((modelMeasurements.get(5).getPpr() + modelMeasurements.get(6).getPpr()) / 2) * 100.0) / 100.0;

        Double[] idealPprs = new Double[2];

        idealPprs[0] = s1.getIdealPpr();
        idealPprs[1] = s2.getIdealPpr();

        Arrays.sort(idealPprs);

        this.idealPpr = idealPprs[1];
        this.pprDiff = Math.round((idealPpr - averagePpr) * 10.0) / 10.0;
    }

    public Model(String brand, String model, String fullName, Sample s1, Sample s2, Sample s3){

        //Set model details
        this.brand = brand;
        this.model = model;
        this.fullName = fullName;

        //Add samples
        this.samples.add(s1);
        this.samples.add(s2);
        this.samples.add(s3);
        
        //Produces an ArrayList of every measurement from each sample in Model's samples
        ArrayList<Measurement> modelMeasurements = getMeasurements();

        //Sort them by PPR to extract median-scoring measurements
        Collections.sort(modelMeasurements, Measurement.CompareByPprAscending);

        //Create a representative dataset for the model from samples
        for(int i = 0; i < modelMeasurements.size(); i++){
            //Representative dataset is the mean average between the two median ppr datasets
            double sampleMagnitude = (modelMeasurements.get(8).getResampledMagnitudes()[i] + modelMeasurements.get(9).getResampledMagnitudes()[i]) / 2;
            this.representativeFrequencyResponse.add(sampleMagnitude);
        }

        //Approximate the PPR of that representative dataset
        this.averagePpr = Math.round(((modelMeasurements.get(8).getPpr() + modelMeasurements.get(9).getPpr()) / 2) * 100.0) / 100.0;

        Double[] idealPprs = new Double[3];

        idealPprs[0] = s1.getIdealPpr();
        idealPprs[1] = s2.getIdealPpr();
        idealPprs[2] = s2.getIdealPpr();

        Arrays.sort(idealPprs);

        this.idealPpr = idealPprs[2];
        this.pprDiff = Math.round((idealPpr - averagePpr) * 10.0) / 10.0;
    }

    public String getFullName() {
        return fullName;
    }

    public String getBrand(){
        return this.brand;
    }

    public String getModel(){
        return this.model;
    }

    public ArrayList<Sample> getSamples() {
        return samples;
    }

    public Double getAveragePpr() {
        return averagePpr;
    }  

    public Double getIdealPpr() {
        return idealPpr;
    }

    public Double getPprDiff() {
        return pprDiff;
    }

    public int getPrice() {
        return price;
    }

    public String getFormat() {
        return format;
    }

    public String getCupType() {
        return cupType;
    }

    public String getDriverType() {
        return driverType;
    }

    public String getNotes() {
        return notes;
    }

    public ArrayList<Double> getRepresentativeFrequencyResponse(){
        return this.representativeFrequencyResponse;
    }

    public ArrayList<Measurement> getMeasurements() {
        ArrayList<Measurement> measurements = new ArrayList<Measurement>();
        for(Sample sample : this.samples){
            ArrayList<Measurement> sampleMeasurements = sample.getMeasurements();
            measurements.addAll(sampleMeasurements);
        }
        return measurements;
    }

    @Override
    public String toString() {
        return  fullName + " Ppr = " + averagePpr + " idealPpr = " + idealPpr;
    }

    public static Comparator<Model> CompareByPprAscending = new Comparator<Model>() {
        public int compare(Model s1, Model s2){
            double s1ppr = s1.getAveragePpr();
            double s2ppr = s2.getAveragePpr();
            if(s1ppr > s2ppr){
                return 1;
            } else if (s1ppr < s2ppr){
                return -1;
            } else {
                return 0;
            }
        }
    };

    public static Comparator<Model> CompareByPprDescending = new Comparator<Model>() {
        public int compare(Model s1, Model s2){
            double s1ppr = s1.getAveragePpr();
            double s2ppr = s2.getAveragePpr();
            if(s1ppr > s2ppr){
                return -1;
            } else if (s1ppr < s2ppr){
                return 1;
            } else {
                return 0;
            }
        }
    };

    public static Comparator<Model> CompareByIdealPprAscending = new Comparator<Model>() {
        public int compare(Model s1, Model s2){
            double s1ppr = s1.getIdealPpr();
            double s2ppr = s2.getIdealPpr();
            if(s1ppr > s2ppr){
                return 1;
            } else if (s1ppr < s2ppr){
                return -1;
            } else {
                return 0;
            }
        }
    };

    public static Comparator<Model> CompareByIdealPprDescending = new Comparator<Model>() {
        public int compare(Model s1, Model s2){
            double s1ppr = s1.getIdealPpr();
            double s2ppr = s2.getIdealPpr();
            if(s1ppr > s2ppr){
                return -1;
            } else if (s1ppr < s2ppr){
                return 1;
            } else {
                return 0;
            }
        }
    };

}