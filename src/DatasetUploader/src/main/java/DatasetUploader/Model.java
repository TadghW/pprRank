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
    private ArrayList<Sample> samples = new ArrayList<Sample>();
    private ArrayList<Double> representativeFrequencyResponse = new ArrayList<Double>();
    private Double averagePpr;
    private Double cupConsistency;
    private int cupConsistencyScore;
    private Double fitConsistency;
    private int fitConsistencyScore;
    private Double sealConsistency;
    private int sealConsistencyScore;
    private Double unitConsistency;
    private int unitConsistencyScore;
    private Boolean comfortIssues = false;
    private int comfortIssuesDebuff = 0;
    private Boolean buildIssues = false;
    private int buildIssuesDebuff  = 0;
    private Double finalScore;
    private String confidence;

    public Model(String brand, String model, String fullName, Sample v1){

        //Set model details
        this.brand = brand;
        this.model = model;
        this.fullName = fullName;

        //Add samples
        samples.add(v1);

        //Produces an ArrayList of every measurement from each sample in Model's samples
        ArrayList<Measurement> modelMeasurements = this.getMeasurements();

        //Sort them by PPR to extract median-scoring measurements
        Collections.sort(modelMeasurements, Measurement.CompareByPprAscending);

        //Create a representative dataset for the model from samples
        for(int i = 0; i < modelMeasurements.get(0).getResampledMagnitudes().size(); i++){
            //Representative dataset is the mean average between the two average ppr datasets
            double sampleMagnitude = (modelMeasurements.get(2).getResampledMagnitudes().get(i) + modelMeasurements.get(3).getResampledMagnitudes().get(i)) / 2;
            this.representativeFrequencyResponse.add(sampleMagnitude);
        }

        //Approximate the PPR of that representative dataset
        this.averagePpr = Math.round(((modelMeasurements.get(2).getPpr() + modelMeasurements.get(3).getPpr()) / 2) * 100.0) /100.0;


        //Cup consistency score of each sample is the absoulute difference in PPR between the average measurements of each cup
        //Models have 1-3 samples, so we're going to average out the scores for each sample to deduce an average model score 

        this.cupConsistency = v1.getCupConsistency();
        this.cupConsistencyScore = v1.getCupConsistencyScore();

        //Fit consistency score of each sample is standard deviation of error of different placements on the test rig from the
        //(sonically) ideal placement. As with before, our samples already have a metric for this and we're going to average
        //out their results.
        
        //We could use a worst case scenario to get model scores but because the majority of headphones only have a single sample
        //this would primarily debuff headphones which have a greater number of samples causing a bias in our rankings
        this.fitConsistency = v1.getFitConsistency();
        this.fitConsistencyScore = v1.getFitConsistencyScore();

        //Seal consistency measurements account for how much low frequency energy is lost between different placements of the same
        //cup of a headphone. We use worst-case scenario here on the sample level and will average that out for multiple sample models
        this.sealConsistency = v1.getSealConsistency();
        this.sealConsistencyScore = v1.getSealConsistencyScore();

        this.finalScore = Math.round((averagePpr + cupConsistencyScore + fitConsistencyScore + sealConsistencyScore) * 10.0) / 10.0;

        this.confidence = "single sample test";
    }


    public Model(String brand, String model, String fullName, Sample v1, Sample v2){

        //Set model details
        this.brand = brand;
        this.model = model;
        this.fullName = fullName;

        //Add samples
        this.samples.add(v1);
        this.samples.add(v2);

        //Produces an ArrayList of every measurement from each sample in Model's samples
        ArrayList<Measurement> modelMeasurements = getMeasurements();

        //Sort them by PPR to extract median-scoring measurements
        Collections.sort(modelMeasurements, Measurement.CompareByPprAscending);

        //Create a representative dataset for the model from samples
        for(int i = 0; i < modelMeasurements.get(0).getResampledMagnitudes().size(); i++){
            //Representative dataset is the mean average between the two median ppr datasets
            double sampleMagnitude = (modelMeasurements.get(5).getResampledMagnitudes().get(i) + modelMeasurements.get(6).getResampledMagnitudes().get(i)) / 2;
            this.representativeFrequencyResponse.add(sampleMagnitude);
        }

        //Approximate the PPR of that representative dataset
        this.averagePpr = Math.round(((modelMeasurements.get(5).getPpr() + modelMeasurements.get(6).getPpr()) / 2) * 100.0) / 100.0;

        //Cup consistency score of each sample is the absoulute difference in PPR between the median measurements of each cup
        //Models have 1-3 samples, so we're going to average out the scores for each sample to deduce an average model score 

        Double cupConsistencies = 0.0;
        
        for(Sample sample : samples){
            cupConsistencies += sample.getCupConsistency();
        }

        this.cupConsistency = Math.round((cupConsistencies / 2) * 100.0) / 100.0;

        if(cupConsistency < 1.5) {
            this.cupConsistencyScore = 1;
        } else if(cupConsistency < 2.9) {
            this.cupConsistencyScore = 0;
        } else if(cupConsistency < 4.5) {
            this.cupConsistencyScore = -1;
        } else if(cupConsistency < 7.5){
            this.cupConsistencyScore = -2;
        } else if(cupConsistency < 10){
            this.cupConsistencyScore = -3;
        } else if(cupConsistency < 15){
            this.cupConsistencyScore = -4;
        } else if(cupConsistency > 15){
            this.cupConsistencyScore = -5;
        }


        //Fit consistency score of each sample is standard deviation of error of different placements on the test rig from the
        //(sonically) ideal placement. As with before, our samples already have a metric for this and we're going to average
        //out their results.
        
        //We could use a worst case scenario to get model scores but because the majority of headphones only have a single sample
        //this would primarily debuff headphones which have a greater number of samples causing a bias in our rankings
        Double fitConsistencies = 0.0;

        for(Sample sample: samples){
            fitConsistencies += sample.getFitConsistency();
        }

        this.fitConsistency = Math.round((fitConsistencies / 2) * 100.0) / 100.0;
        
        if(fitConsistency < 1){
            this.fitConsistencyScore = 1;
        } else if(fitConsistency < 2){
            this.fitConsistencyScore = 0;
        } else if(fitConsistency < 3){
            this.fitConsistencyScore = -1;
        } else if(fitConsistency < 4){
            this.fitConsistencyScore = -2;
        } else if(fitConsistency < 5){
            this.fitConsistencyScore = -3;
        } else if(fitConsistency > 5){
            this.fitConsistencyScore = -4;
        }

        //Seal consistency measurements account for how much low frequency energy is lost between different placements of the same
        //cup of a headphone. We use worst-case scenario here on the sample level and will average that out for multiple sample models
        Double leakageQuants = 0.0;

        for(Sample sample: samples){
            leakageQuants += sample.getSealConsistency();
        }

        this.sealConsistency = Math.round((leakageQuants / 2) * 100.0) / 100.0;

        if(sealConsistency > -2){
            sealConsistencyScore = 1;
        } else if(sealConsistency > -4){
            sealConsistencyScore = 0;
        } else if(sealConsistency > -6){
            sealConsistencyScore = -1;
        } else if(sealConsistency > -10) {
            sealConsistencyScore = -2;
        } else if(sealConsistency > -15) {
            sealConsistencyScore = -3;
        } else if(sealConsistencyScore < -15) {
            sealConsistencyScore = -4;
        }

        //Unit consistency is an added metric for multiple sample models. It rewards multi-sample headphones with consistent average
        //performance between headphones. It debuffs multi sample models with particularly poor consistency.
        this.unitConsistency = Math.round(Math.abs(v1.getMedianPpr() - v2.getMedianPpr()) * 100.0) / 100.0;

        if(unitConsistency < 3){
            unitConsistencyScore = 1;
        } else if(unitConsistency < 8){
            unitConsistencyScore = 0;
        } else if(unitConsistency < 12){
            unitConsistencyScore = -1;
        }

        this.finalScore = Math.round((averagePpr + cupConsistencyScore + fitConsistencyScore + sealConsistencyScore + unitConsistencyScore) * 10.0) / 10.0;

        this.confidence = "2 unit samples";
    }

    public Model(String brand, String model, String fullName, Sample v1, Sample v2, Sample v3){

        //Set model details
        this.brand = brand;
        this.model = model;
        this.fullName = fullName;

        //Add samples
        this.samples.add(v1);
        this.samples.add(v2);
        this.samples.add(v3);
        
        //Produces an ArrayList of every measurement from each sample in Model's samples
        ArrayList<Measurement> modelMeasurements = getMeasurements();

        //Sort them by PPR to extract median-scoring measurements
        Collections.sort(modelMeasurements, Measurement.CompareByPprAscending);

        //Create a representative dataset for the model from samples
        for(int i = 0; i < modelMeasurements.size(); i++){
            //Representative dataset is the mean average between the two median ppr datasets
            double sampleMagnitude = (modelMeasurements.get(8).getResampledMagnitudes().get(i) + modelMeasurements.get(9).getResampledMagnitudes().get(i)) / 2;
            this.representativeFrequencyResponse.add(sampleMagnitude);
        }

        //Approximate the PPR of that representative dataset
        this.averagePpr = Math.round(((modelMeasurements.get(8).getPpr() + modelMeasurements.get(9).getPpr()) / 2) * 100.0) / 100.0;

        //Cup consistency score of each sample is the absoulute difference in PPR between the median measurements of each cup
        //Models have 1-3 samples, so we're going to average out the scores for each sample to deduce an average model score 
        Double cupConsistencies = 0.0;
        
        for(Sample sample : samples){
            cupConsistencies += sample.getCupConsistency();
        }

        this.cupConsistency = Math.round((cupConsistencies / 3) * 100.0) / 100.0;

        if(cupConsistency < 1.5) {
            this.cupConsistencyScore = 1;
        } else if(cupConsistency < 2.9) {
            this.cupConsistencyScore = 0;
        } else if(cupConsistency < 4.5) {
            this.cupConsistencyScore = -1;
        } else if(cupConsistency < 7.5){
            this.cupConsistencyScore = -2;
        } else if(cupConsistency < 10){
            this.cupConsistencyScore = -3;
        } else if(cupConsistency < 15){
            this.cupConsistencyScore = -4;
        } else if(cupConsistency > 15){
            this.cupConsistencyScore = -5;
        }

        //Fit consistency score of each sample is standard deviation of error of different placements on the test rig from the
        //(sonically) ideal placement. As with before, our samples already have a metric for this and we're going to average
        //out their results.
        
        //We could use a worst case scenario to get model scores but because the majority of headphones only have a single sample
        //this would primarily debuff headphones which have a greater number of samples causing a bias in our rankings
        Double fitConsistencies = 0.0;

        for(Sample sample : samples){
            fitConsistencies += sample.getFitConsistency();
        }

        this.fitConsistency = Math.round((fitConsistencies / 3) * 100.0) / 100.0;
        
        if(fitConsistency < 1){
            this.fitConsistencyScore = 1;
        } else if(fitConsistency < 2){
            this.fitConsistencyScore = 0;
        } else if(fitConsistency < 3){
            this.fitConsistencyScore = -1;
        } else if(fitConsistency < 4){
            this.fitConsistencyScore = -2;
        } else if(fitConsistency < 5){
            this.fitConsistencyScore = -3;
        } else if(fitConsistency > 5){
            this.fitConsistencyScore = -4;
        }

        //Seal consistency measurements account for how much low frequency energy is lost between different placements of the same
        //cup of a headphone. We use worst-case scenario here on the sample level and will average that out for multiple sample models
        Double leakageQuants = 0.0;

        for(Sample sample : samples){
            leakageQuants += sample.getSealConsistency();
        }

        this.sealConsistency = Math.round((leakageQuants / 3) * 100.0) / 100.0;;

        if(sealConsistency > -2){
            sealConsistencyScore = 1;
        } else if(sealConsistency > -4){
            sealConsistencyScore = 0;
        } else if(sealConsistency > -6){
            sealConsistencyScore = -1;
        } else if(sealConsistency > -10) {
            sealConsistencyScore = -2;
        } else if(sealConsistency > -15) {
            sealConsistencyScore = -3;
        } else if(sealConsistencyScore < -15) {
            sealConsistencyScore = -4;
        }

        //Unit consistency is an added metric for multiple sample models. It rewards multi-sample headphones with consistent average
        //performance between headphones. It debuffs multi sample models with particularly poor consistency.
        Double diff1 = Math.abs(v1.getMedianPpr() - v2.getMedianPpr());
        Double diff2 = Math.abs(v1.getMedianPpr() - v3.getMedianPpr());
        Double diff3 = Math.abs(v2.getMedianPpr() - v1.getMedianPpr());
        Double diff4 = Math.abs(v2.getMedianPpr() - v3.getMedianPpr());
        Double diff5 = Math.abs(v3.getMedianPpr() - v1.getMedianPpr());
        Double diff6 = Math.abs(v3.getMedianPpr() - v2.getMedianPpr());

        this.unitConsistency = Math.round(((diff1 + diff2 + diff3 + diff4 + diff5 + diff6) / 6) * 100.0) / 100.0;

        if(unitConsistency < 4.5){
            unitConsistencyScore = 1;
        } else if(unitConsistency < 8){
            unitConsistencyScore = 0;
        } else if(unitConsistency < 12){
            unitConsistencyScore = -1;
        }

        this.finalScore = Math.round((averagePpr + cupConsistencyScore + fitConsistencyScore + sealConsistencyScore + unitConsistencyScore) * 10.0) / 10.0;

        this.confidence = "3 unit samples";
    }

    public String getFullName() {
        return fullName;
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

    public Boolean getComfortIssues() {
        return comfortIssues;
    }

    public int getComfortIssuesDebuff() {
        return comfortIssuesDebuff;
    }

    public Boolean getBuildIssues() {
        return buildIssues;
    }

    public int getBuildIssuesDebuff() {
        return buildIssuesDebuff;
    }

    public Double getFinalScore() {
        return finalScore;
    }

    public String getBrand(){
        return this.brand;
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

    public String getConfidence(){
        return this.confidence;
    }

    @Override
    public String toString() {
        return  fullName + "\r\naveragePpr = " + averagePpr + ", cupConsistency = " + cupConsistency + "(" + cupConsistencyScore + "), fitConsistency = " + fitConsistency + "(" + fitConsistencyScore + "), sealConsistency="
                + sealConsistency + "(" + sealConsistencyScore + "), unitConsistency: " + unitConsistency + "(" + unitConsistencyScore + ")\r\nFinal Score: " + finalScore + " Confidence: " + confidence;
    } 

    public static Comparator<Model> CompareByPprAscending = new Comparator<Model>() {
        public int compare(Model v1, Model v2){
            double v1ppr = v1.getAveragePpr();
            double v2ppr = v2.getAveragePpr();
            if(v1ppr > v2ppr){
                return 1;
            } else if (v1ppr < v2ppr){
                return -1;
            } else {
                return 0;
            }
        }
    };

    public static Comparator<Model> CompareByPprDescending = new Comparator<Model>() {
        public int compare(Model v1, Model v2){
            double v1ppr = v1.getAveragePpr();
            double v2ppr = v2.getAveragePpr();
            if(v1ppr > v2ppr){
                return -1;
            } else if (v1ppr < v2ppr){
                return 1;
            } else {
                return 0;
            }
        }
    };

    public static Comparator<Model> CompareByCupConsistencyAscending = new Comparator<Model>(){
        public int compare(Model v1, Model v2){
            double v1cup = v1.getCupConsistency();
            double v2cup = v2.getCupConsistency();
            if(v1cup > v2cup){
                return 1;
            } else if (v1cup < v2cup){
                return -1;
            } else {
                return 0;
            }
        }
    };

    public static Comparator<Model> CompareByCupConsistencyDescending = new Comparator<Model>(){
        public int compare(Model v1, Model v2){
            double v1cup = v1.getCupConsistency();
            double v2cup = v2.getCupConsistency();
            if(v1cup > v2cup){
                return -1;
            } else if (v1cup < v2cup){
                return 1;
            } else {
                return 0;
            }
        }
    };

    public static Comparator<Model> CompareByFitConsistencyAscending = new Comparator<Model>(){
        public int compare(Model v1, Model v2){
            double v1seat = v1.getCupConsistency();
            double v2seat = v2.getCupConsistency();
            if(v1seat > v2seat){
                return 1;
            } else if (v1seat < v2seat){
                return -1;
            } else {
                return 0;
            }
        }
    };

    public static Comparator<Model> CompareByFitConsistencyDescending = new Comparator<Model>(){
        public int compare(Model v1, Model v2){
            double v1seat = v1.getFitConsistency();
            double v2seat = v2.getFitConsistency();
            if(v1seat > v2seat){
                return -1;
            } else if (v1seat < v2seat){
                return 1;
            } else {
                return 0;
            }
        }
    };

    public static Comparator<Model> CompareByBassLeakAscending = new Comparator<Model>(){
        public int compare(Model v1, Model v2){
            double v1leak = v1.getSealConsistency();
            double v2leak = v2.getSealConsistency();
            if(v1leak > v2leak){
                return 1;
            } else if (v1leak < v2leak){
                return -1;
            } else {
                return 0;
            }
        }
    };

    public static Comparator<Model> CompareByBassLeakDescending = new Comparator<Model>(){
        public int compare(Model v1, Model v2){
            double v1leak = v1.getSealConsistency();
            double v2leak = v2.getSealConsistency();
            if(v1leak > v2leak){
                return -1;
            } else if (v1leak < v2leak){
                return 1;
            } else {
                return 0;
            }
        }
    };

    public static Comparator<Model> CompareByFinalScoreAscending = new Comparator<Model>(){
        public int compare(Model v1, Model v2){
            double v1score = v1.getFinalScore();
            double v2score = v2.getFinalScore();
            if(v1score > v2score){
                return 1;
            } else if (v1score < v2score){
                return -1;
            } else {
                return 0;
            }
        }
    };

    public static Comparator<Model> CompareByFinalScoreDescending = new Comparator<Model>(){
        public int compare(Model v1, Model v2){
            double v1score = v1.getFinalScore();
            double v2score = v2.getFinalScore();
            if(v1score > v2score){
                return -1;
            } else if (v1score < v2score){
                return 1;
            } else {
                return 0;
            }
        }
    };

}