package main.java.DatasetUploader;

import java.util.Collections;
import java.util.Objects;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.stream.Stream;
import main.java.DatasetUploader.Measurement;
import java.util.Comparator;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

public class Sample {

    private String brand;
    private String variant;
    private String fullName;
    private Measurement l1;
    private Measurement l2;
    private Measurement l3;
    private Measurement r1;
    private Measurement r2;
    private Measurement r3;
    private Measurement[] measurements = new Measurement[6];
    private Double averagePpr;
    private Double cupConsistency;
    private int cupConsistencyScore;
    private Double fitConsistency;
    private int fitConsistencyScore;
    private Double sealConsistency;
    private int sealConsistencyScore;
    private Double finalScore;

    public Sample(String brand, String variant, String fullName, Measurement l1, Measurement l2, Measurement l3, Measurement r1, Measurement r2, Measurement r3){
        
        this.brand = brand;
        this.variant = variant;
        this.fullName = fullName;
        this.l1 = l1;
        this.l2 = l2;
        this.l3 = l3;
        this.r1 = r1;
        this.r2 = r2;
        this.r3 = r3;
        this.measurements[0] = l1;
        this.measurements[1] = l2;
        this.measurements[2] = l3;
        this.measurements[3] = r1;
        this.measurements[4] = r2;
        this.measurements[5] = r3; 
        
        double[] leftSidePpr = {this.l1.getPpr(), this.l2.getPpr(), this.l3.getPpr()};
        double[] rightSidePpr = {this.r1.getPpr(), this.r2.getPpr(), this.r3.getPpr()};
        Arrays.sort(leftSidePpr);
        Arrays.sort(rightSidePpr);
        double[] totalPpr = {this.l1.getPpr(), this.l2.getPpr(), this.l3.getPpr(), this.r1.getPpr(), this.r2.getPpr(), this.r3.getPpr()};
        Arrays.sort(totalPpr);
        
        
        //Average PPR is our highest weighted and headline figure, it's the mean of the two median measurements per sample
        this.averagePpr = Math.round(((totalPpr[2] + totalPpr[3]) / 2) * 100.0) / 100.0;
        
        //Cup consistency measures the difference between each cup's average ppr
        double leftSideAverage = 0;
        double rightSideAverage = 0;
        for(int i = 0; i < 3; i++){
            leftSideAverage += leftSidePpr[i];
            rightSideAverage += rightSidePpr[i];
        }
        leftSideAverage = leftSideAverage / 3;
        rightSideAverage = rightSideAverage / 3;

        this.cupConsistency = Math.round(Math.abs(leftSideAverage - rightSideAverage) * 100.0) / 100.0;

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

        
        //Fit consistency measures the standard deviation of seatings to ideal by cup and averages them to reach average
        //standard deviation from average PPR by placement. 
        double[] errorCurveLeft = new double[3];
        double[] errorCurveRight = new double[3];

        for(int i = 0; i != 3; i++){
            errorCurveLeft[i] = Math.abs(leftSideAverage - leftSidePpr[i]);
            errorCurveRight[i] = Math.abs(rightSideAverage - rightSidePpr[i]);
        }

        StandardDeviation standardDeviation = new StandardDeviation(true);
        Double leftStDev = standardDeviation.evaluate(errorCurveLeft);
        Double rightStDev = standardDeviation.evaluate(errorCurveRight); 

        this.fitConsistency = Math.round((leftStDev + rightStDev / 2) * 100.0) / 100.0;

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

        //Seal consistency measures how much low frequency energy a sample loses across its different seatings across a
        //limited frequency range (30Hz to 200Hz)
        double[] leftSideBassQuants = new double[3];
        
        for(int i = 0; i < 3; i++){
            ArrayList<Double> resampledMagnitudes = measurements[i].getResampledMagnitudes();
            double datasetBassQuant = 0;
            for(int j = 9; j < 40; j++){
                datasetBassQuant += resampledMagnitudes.get(j);
            }
            leftSideBassQuants[i] = datasetBassQuant;
        }
        
        double[] rightSideBassQuants = new double[3];

        for(int i = 3; i > 6; i++){
            ArrayList<Double> resampledMagnitudes = measurements[i].getResampledMagnitudes();

            double datasetBassQuant = 0;
            for(int j = 0; j < 40; j++){
                datasetBassQuant += resampledMagnitudes.get(j);
            }
            rightSideBassQuants[i - 3] = datasetBassQuant;
        }
        
        double leftBestSeatBassQuant = Arrays.stream(leftSideBassQuants).max().orElseThrow(IllegalStateException::new);
        double leftWorstSeatBassQuant = Arrays.stream(leftSideBassQuants).min().orElseThrow(IllegalStateException::new);
        double rightBestSeatBassQuant = Arrays.stream(rightSideBassQuants).max().orElseThrow(IllegalStateException::new);
        double rightWorstSeatBassQuant = Arrays.stream(rightSideBassQuants).min().orElseThrow(IllegalStateException::new);
        double leftBassLeak = Math.round((leftWorstSeatBassQuant - leftBestSeatBassQuant) * 100.0) / 100.0;
        double rightBassLeak = Math.round((rightWorstSeatBassQuant - rightBestSeatBassQuant) * 100.0) / 100.0;
        
        if(leftBassLeak < rightBassLeak){
            this.sealConsistency = leftBassLeak;
        } else {
            this.sealConsistency = rightBassLeak;
        }

        if(sealConsistency > -2){
            this.sealConsistencyScore = 1;
        } else if(sealConsistency > -4){
            this.sealConsistencyScore = 0;
        } else if(sealConsistency > -6){
            this.sealConsistencyScore = -1;
        } else if(sealConsistency > -10) {
            this.sealConsistencyScore = -2;
        } else if(sealConsistency > -15) {
            this.sealConsistencyScore = -3;
        } else {
            this.sealConsistencyScore = -4;
        }
        

    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getSample() {
        return variant;
    }

    public String getFullName() {
        return fullName;
    }

    public String getModel() {
        return this.variant;
    }

    public Double getMedianPpr() {
        return averagePpr;
    }

    public Double getCupConsistency() {
        return cupConsistency;
    }

    public Double getFitConsistency() {
        return fitConsistency;
    }

    public int getFitConsistencyScore(){
        return fitConsistencyScore;
    }

    public Double getSealConsistency() {
        return sealConsistency;
    }

    public int getCupConsistencyScore(){
        return this.cupConsistencyScore;
    }

    public int getSealConsistencyScore(){
        return sealConsistencyScore;
    }
    
    public Double getFinalScore() {
        return finalScore;
    }

    public Measurement getMeasurement(int dataset){
        switch(Integer.valueOf(dataset)){
            case 0 : 
                return this.l1;
            case 1 : 
                return this.l2;
            case 2: 
                return this.l3;
            case 3: 
                return this.r1;
            case 4 :
                return this.r2;
            case 5 : 
                return this.r3;
            default : 
                System.out.println("Problem finding Measurement! Printing null dataset - Instability inbound");
                Measurement nullMeasurement = new Measurement();
                return nullMeasurement;
        }
    }

    public ArrayList<Measurement> getMeasurements(){
        ArrayList<Measurement> measurements = new ArrayList<Measurement>();
        measurements.add(this.l1);
        measurements.add(this.l2);
        measurements.add(this.l3);
        measurements.add(this.r1);
        measurements.add(this.r2);
        measurements.add(this.r3);
        return measurements;
    }

    @Override
    public String toString() {
        return  fullName + "\r\naveragePpr = " + averagePpr + ", cupConsistency = " + cupConsistency + "(" + cupConsistencyScore + "), fitConsistency = " + fitConsistency + "(" + fitConsistencyScore + "), sealConsistency="
                + sealConsistency + "(" + sealConsistencyScore + ")" + "\r\nFinal Score: " + finalScore;
    }

    public static Comparator<Sample> CompareByPprAscending = new Comparator<Sample>() {
        public int compare(Sample v1, Sample v2){
            double v1ppr = v1.getMedianPpr();
            double v2ppr = v2.getMedianPpr();
            if(v1ppr > v2ppr){
                return 1;
            } else if (v1ppr < v2ppr){
                return -1;
            } else {
                return 0;
            }
        }
    };

    public static Comparator<Sample> CompareByPprDescending = new Comparator<Sample>() {
        public int compare(Sample v1, Sample v2){
            double v1ppr = v1.getMedianPpr();
            double v2ppr = v2.getMedianPpr();
            if(v1ppr > v2ppr){
                return -1;
            } else if (v1ppr < v2ppr){
                return 1;
            } else {
                return 0;
            }
        }
    };

    public static Comparator<Sample> CompareByCupConsistencyAscending = new Comparator<Sample>(){
        public int compare(Sample v1, Sample v2){
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

    public static Comparator<Sample> CompareByCupConsistencyDescending = new Comparator<Sample>(){
        public int compare(Sample v1, Sample v2){
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

    public static Comparator<Sample> CompareByFitConsistencyAscending = new Comparator<Sample>(){
        public int compare(Sample v1, Sample v2){
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

    public static Comparator<Sample> CompareByFitConsistencyDescending = new Comparator<Sample>(){
        public int compare(Sample v1, Sample v2){
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

    public static Comparator<Sample> CompareByBassLeakAscending = new Comparator<Sample>(){
        public int compare(Sample v1, Sample v2){
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

    public static Comparator<Sample> CompareByBassLeakDescending = new Comparator<Sample>(){
        public int compare(Sample v1, Sample v2){
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

    public static Comparator<Sample> CompareByFinalScoreAscending = new Comparator<Sample>(){
        public int compare(Sample v1, Sample v2){
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

    public static Comparator<Sample> CompareByFinalScoreDescending = new Comparator<Sample>(){
        public int compare(Sample v1, Sample v2){
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