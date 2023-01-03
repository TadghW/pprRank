package main.java.DatasetUploader;

import java.util.*;
import java.lang.Math;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

public class Measurement {

    private String brand;
    private String model;
    private String fullName;
    private char side;
    private int seating;
    private double[] originalFrequencies;
    private double[] originalMagnitudes;
    private Double[] resampledMagnitudes;
    private Double ppr;

    public Measurement(){};

    public Measurement(String brand, String model, String fullName, String side, String seating, ArrayList<Double> originalFrequencies, ArrayList<Double> originalMagnitudes, ArrayList<Double> originalPhase){

        //The raw exports in this measurement are inconvenient for our web app in a number of ways.
        //First of all, they capture frequency samples as low as 10Hz, frequencies that are
        //inconsequential for headphones. Secondly, they're higher resoution than we need, and
        //they use different samples than we do. We could just use magnitude samples from the 
        //nearest sample to our preferred sample locations but we can do better.

        //What we're going to do instead is look at each sample in our preferred frequency set,
        //identify any direct matches where we can lift the mangitude data wholesale from the 
        //measuremment dataset. When we don't have a matching sample. we're going to identify
        //the samples in the measurement that surround the preferred sample in our set and 
        //use that data in a linear interpolation algorithm to grab a different sample from the
        //line described by the original dataset

        this.brand = brand;
        this.model = model;
        this.fullName = fullName;
        this.side = side.charAt(0);
        this.seating = Integer.valueOf(seating);

        ArrayList<Double> interpolatedMags = new ArrayList<Double>();

        this.originalFrequencies = new double[originalFrequencies.size()];
        this.originalMagnitudes = new double[originalFrequencies.size()];
        this.resampledMagnitudes = new Double[Constants.preferredFrequencies.length];

        for(int i=0; i < this.originalFrequencies.length ; i++){
            this.originalFrequencies[i] = originalFrequencies.get(i);
            this.originalMagnitudes[i] = originalMagnitudes.get(i);
        }

        LinearInterpolator lerp = new LinearInterpolator();
        PolynomialSplineFunction magSpline = lerp.interpolate(this.originalFrequencies, this.originalMagnitudes);
        
        for(int i = 0; i < Constants.preferredFrequencies.length; i++){
            interpolatedMags.add(magSpline.value(Constants.preferredFrequencies[i]));
        }
        
        Double fiveHundredHzMag = interpolatedMags.get(56);

        for(int i = 0; i < Constants.preferredFrequencies.length; i ++){
            Double resampledMag = Math.round((interpolatedMags.get(i) - fiveHundredHzMag) * 100.0) / 100.0;
            this.resampledMagnitudes[i] = resampledMag;
        }

        this.ppr = Constants.calculatePpr(this.resampledMagnitudes);

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
    
    public char getSide() {
        return side;
    }
    
    public int getSeating() {
        return seating;
    }
    
    public double[] getOriginalFrequencies() {
        return originalFrequencies;
    }
    
    public double[] getOriginalMagnitudes() {
        return originalMagnitudes;
    }

    public Double[] getResampledMagnitudes() {
        return resampledMagnitudes;
    }
    
    public Double getPpr() {
        return ppr;
    }

    @Override
    public String toString() {
        return "brand = " + brand + ", model=" + model + ", side=" + side
                + ", seating=" + seating + " ppr=" + ppr;
    
    }

    public static Comparator<Measurement> CompareByPprAscending = new Comparator<Measurement>() {
        public int compare(Measurement v1, Measurement v2){
            Double v1ppr = v1.getPpr();
            Double v2ppr = v2.getPpr();
            if(v1ppr > v2ppr){
                return 1;
            } else if (v1ppr < v2ppr){
                return -1;
            } else {
                return 0;
            }
        }
    };
}