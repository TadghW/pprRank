package main.java.DatasetUploader;

import java.util.Collections;
import java.util.Objects;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.stream.Stream;
import main.java.DatasetUploader.Dataset;
import java.util.Comparator;

public class Variant {

    private String brand;
    private String variant;
    private String fullName;
    private Dataset l1;
    private Dataset l2;
    private Dataset l3;
    private Dataset r1;
    private Dataset r2;
    private Dataset r3;
    private Dataset[] datasets = new Dataset[6];

    //Metrics:
    //1) Headline PPR Score : All dataset pprs in order, taking the mean of the middle two values.  
    //2) Maximum PPR Variance: A score of how much PPR can vary by seating.
    //3) PPR Variation by Side: A measurement of cup consistency.
    //4) PPR Variation by Seat: A measure of seating consistency. 
    //5) Average bass frequency leakage: Average loss from 200Hz to 20Hz from poor seal.

    private Double medianPpr;
    private Double potentialVariance;
    private Double cupConsistency;
    private Double seatConsistency;
    private Double sumBassLeakage;

    public Variant(String brand, String variant, String fullName, Dataset l1, Dataset l2, Dataset l3, Dataset r1, Dataset r2, Dataset r3){
        this.brand = brand;
        this.variant = variant;
        this.fullName = fullName;
        this.l1 = l1;
        this.l2 = l2;
        this.l3 = l3;
        this.r1 = r1;
        this.r2 = r2;
        this.r3 = r3;
        this.datasets[0] = l1;
        this.datasets[1] = l2;
        this.datasets[2] = l3;
        this.datasets[3] = r1;
        this.datasets[4] = r2;
        this.datasets[5] = r3; 
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getVariant() {
        return variant;
    }

    public void setVariant(String variant) {
        this.variant = variant;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Dataset[] getDatasets() {
        return datasets;
    }

    public void setDatasets(Dataset[] datasets) {
        this.datasets = datasets;
    }

    public Double getMedianPpr() {
        return medianPpr;
    }

    public void setMedianPpr(Double medianPpr) {
        this.medianPpr = medianPpr;
    }

    public Double getPotentialVariance() {
        return potentialVariance;
    }

    public void setPotentialVariance(Double potentialVariance) {
        this.potentialVariance = potentialVariance;
    }

    public Double getCupConsistency() {
        return cupConsistency;
    }

    public void setCupConsistency(Double cupConsistency) {
        this.cupConsistency = cupConsistency;
    }

    public Double getSeatConsistency() {
        return seatConsistency;
    }

    public void setSeatConsistency(Double seatConsistency) {
        this.seatConsistency = seatConsistency;
    }

    public Double getSumBassLeakage() {
        return sumBassLeakage;
    }

    public void setSumBassLeakage(Double sumBassLeakage) {
        this.sumBassLeakage = sumBassLeakage;
    }

    public void rateVariant(){

        //Sort measurements into side arrays and an all seating array
        double[] leftSidePpr = {this.l1.getPpr(), this.l2.getPpr(), this.l3.getPpr()};
        double[] rightSidePpr = {this.r1.getPpr(), this.r2.getPpr(), this.r3.getPpr()};

        Arrays.sort(leftSidePpr);
        Arrays.sort(rightSidePpr);

        double[] totalPpr = {this.l1.getPpr(), this.l2.getPpr(), this.l3.getPpr(), this.r1.getPpr(), this.r2.getPpr(), this.r3.getPpr()};

        Arrays.sort(totalPpr);

        //Get headline PPR figure
        this.medianPpr = Math.round(((totalPpr[2] + totalPpr[3]) / 2) * 100.0) / 100.0;
        
        //Set potential variance figure
        this.potentialVariance = Math.round(Math.abs(totalPpr[0] - totalPpr[5]) * 100.0) / 100.0;

        //Set cup consistency
        this.cupConsistency = Math.round(Math.abs(rightSidePpr[1] - leftSidePpr[1]) * 100.0) / 100.0;

        double highestPprLeft = Arrays.stream(leftSidePpr).max().orElseThrow(IllegalStateException::new);
        double lowestPprLeft = Arrays.stream(leftSidePpr).min().orElseThrow(IllegalStateException::new);
        double highestPprRight = Arrays.stream(rightSidePpr).max().orElseThrow(IllegalStateException::new);
        double lowestPprRight = Arrays.stream(rightSidePpr).min().orElseThrow(IllegalStateException::new);
        double maxVarianceLeft = highestPprLeft - lowestPprLeft;
        double maxVarianceRight = highestPprRight - lowestPprRight;

        //Get seating consistency
        if(maxVarianceLeft > maxVarianceRight){
            this.seatConsistency = Math.round(maxVarianceLeft * 100.0) / 100.0;
        } else {
            this.seatConsistency = Math.round(maxVarianceRight * 100.0) / 100.0;
        }

        //Get bass quantities for each cup
        double[] leftSideBassQuants = new double[3];

        for(int i = 0; i < 3; i++){

            ArrayList<Double> resampledMagnitudes = datasets[i].getResampledMagnitudes();
            
            double datasetBassQuant = 0;

            for(int j = 0; j < 40; j++){
                datasetBassQuant += resampledMagnitudes.get(j);
            }

            leftSideBassQuants[i] = datasetBassQuant;

        }

        double[] rightSideBassQuants = new double[3];

        for(int i = 3; i > 6; i++){

            ArrayList<Double> resampledMagnitudes = datasets[i].getResampledMagnitudes();
            
            double datasetBassQuant = 0;

            for(int j = 0; j < 40; j++){
                datasetBassQuant += resampledMagnitudes.get(j);
            }

            rightSideBassQuants[i - 3] = datasetBassQuant;

        }

        //Figure out which cup has the worst bass leak by best vs worst seating
        double leftBestSeatBassQuant = Arrays.stream(leftSideBassQuants).max().orElseThrow(IllegalStateException::new);
        double leftWorstSeatBassQuant = Arrays.stream(leftSideBassQuants).min().orElseThrow(IllegalStateException::new);
        double rightBestSeatBassQuant = Arrays.stream(rightSideBassQuants).max().orElseThrow(IllegalStateException::new);
        double rightWorstSeatBassQuant = Arrays.stream(rightSideBassQuants).min().orElseThrow(IllegalStateException::new);

        double leftBassLeak = Math.round((leftWorstSeatBassQuant - leftBestSeatBassQuant) * 100.0) / 100.0;
        double rightBassLeak = Math.round((rightWorstSeatBassQuant - rightBestSeatBassQuant) * 100.0) / 100.0;

        //Set sumBassLeakage
        if(leftBassLeak < rightBassLeak){
            this.sumBassLeakage = leftBassLeak;
        } else {
            this.sumBassLeakage = rightBassLeak;
        }
    }

    @Override
    public String toString() {
        return "Variant : " + fullName + /*"\r\nL1 is null = " + Objects.isNull(l1) + "\r\nL2 is null = "
                + Objects.isNull(l2) + " \r\nL3 is null = " + Objects.isNull(l3) + "\r\nR1 is null = " + Objects.isNull(r1) + "\r\nR2 is null = " + Objects.isNull(r2) + "\r\nR3 is null = " + Objects.isNull(r3) + "\r\n"
                +*/ "\r\nmedianPpr=" + medianPpr + ", potentialVariance=" + potentialVariance + ", cupConsistency=" + cupConsistency + ", seatConsistency=" + seatConsistency + ", sumBassLeakage="
                + sumBassLeakage + "]";
    }

    public static Comparator<Variant> CompareByPprAscending = new Comparator<Variant>() {
        public int compare(Variant v1, Variant v2){
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

    public static Comparator<Variant> CompareByPprDescending = new Comparator<Variant>() {
        public int compare(Variant v1, Variant v2){
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
    
}