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
    private String model;
    private String fullName;
    private Measurement l1;
    private Measurement l2;
    private Measurement l3;
    private Measurement r1;
    private Measurement r2;
    private Measurement r3;
    private Measurement[] measurements = new Measurement[6];
    private ArrayList<Double> averageDataset = new ArrayList<Double>();
    private Double ppr;
    private Double idealPpr;
    private Double pprDiff;

    public Sample(String brand, String model, String fullName, Measurement l1, Measurement l2, Measurement l3, Measurement r1, Measurement r2, Measurement r3){
        
        this.brand = brand;
        this.model = model;
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
        
        for(int i = 0; i < Constants.preferredFrequencies.length; i++){
            double totalMag = 0;
            totalMag += l1.getResampledMagnitudes()[i];
            totalMag += l2.getResampledMagnitudes()[i];
            totalMag += l3.getResampledMagnitudes()[i];
            totalMag += r1.getResampledMagnitudes()[i];
            totalMag += r2.getResampledMagnitudes()[i];
            totalMag += r3.getResampledMagnitudes()[i];
            double averagedMag = totalMag / 6;
            averageDataset.add(averagedMag);
        }

        Double[] averageDatasetArray = averageDataset.toArray(new Double[averageDataset.size()]);
        this.ppr = Constants.calculatePpr(averageDatasetArray);
        Arrays.sort(measurements, Measurement.CompareByPprAscending);
        this.idealPpr = measurements[5].getPpr();
        this.pprDiff = idealPpr - ppr;

    }

    public Sample(String brand, String model, String fullName, Measurement l1, Measurement r1){
        
        this.brand = brand;
        this.model = model;
        this.fullName = fullName;
        this.l1 = l1;
        this.l2 = l1;
        this.l3 = l1;
        this.r1 = r1;
        this.r2 = r1;
        this.r3 = r1;
        this.measurements[0] = l1;
        this.measurements[1] = l2;
        this.measurements[2] = l3;
        this.measurements[3] = r1;
        this.measurements[4] = r2;
        this.measurements[5] = r3; 
        
        for(int i = 0; i < Constants.preferredFrequencies.length; i++){
            double totalMag = 0;
            totalMag += l1.getResampledMagnitudes()[i];
            totalMag += l2.getResampledMagnitudes()[i];
            totalMag += l3.getResampledMagnitudes()[i];
            totalMag += r1.getResampledMagnitudes()[i];
            totalMag += r2.getResampledMagnitudes()[i];
            totalMag += r3.getResampledMagnitudes()[i];
            double averagedMag = totalMag / 6;
            averageDataset.add(averagedMag);
        }

        Double[] averageDatasetArray = averageDataset.toArray(new Double[averageDataset.size()]);
        this.ppr = Constants.calculatePpr(averageDatasetArray);
        Arrays.sort(measurements, Measurement.CompareByPprAscending);
        this.idealPpr = measurements[5].getPpr();
        this.pprDiff = idealPpr - ppr;

    }

    public Sample(String brand, String model, String fullName, Measurement a1){
        
        this.brand = brand;
        this.model = model;
        this.fullName = fullName;
        this.l1 = a1;
        this.l2 = a1;
        this.l3 = a1;
        this.r1 = a1;
        this.r2 = a1;
        this.r3 = a1;
        this.measurements[0] = l1;
        this.measurements[1] = l2;
        this.measurements[2] = l3;
        this.measurements[3] = r1;
        this.measurements[4] = r2;
        this.measurements[5] = r3;

        this.ppr = a1.getPpr();

    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getSample() {
        return model;
    }

    public String getFullName() {
        return fullName;
    }

    public String getModel() {
        return model;
    }

    public Double getPpr() {
        return ppr;
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
                System.out.println("Warning: returning null Measurement object");
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

    public Double getIdealPpr() {
        return idealPpr;
    }

    public Double getPprDiff() {
        return pprDiff;
    }

    @Override
    public String toString() {
        return  fullName + " ppr = " + ppr;
    }

    public static Comparator<Sample> CompareByPprAscending = new Comparator<Sample>() {
        public int compare(Sample s1, Sample s2){
            double s1ppr = s1.getPpr();
            double s2ppr = s2.getPpr();
            if(s1ppr > s2ppr){
                return 1;
            } else if (s1ppr < s2ppr){
                return -1;
            } else {
                return 0;
            }
        }
    };

    public static Comparator<Sample> CompareByIdealPprAscending = new Comparator<Sample>() {
        public int compare(Sample s1, Sample s2){
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

    public static Comparator<Sample> CompareByPprDescending = new Comparator<Sample>() {
        public int compare(Sample s1, Sample s2){
            double s1ppr = s1.getPpr();
            double s2ppr = s2.getPpr();
            if(s1ppr > s2ppr){
                return -1;
            } else if (s1ppr < s2ppr){
                return 1;
            } else {
                return 0;
            }
        }
    };

    public static Comparator<Sample> CompareByIdealPprDescending = new Comparator<Sample>() {
        public int compare(Sample s1, Sample s2){
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