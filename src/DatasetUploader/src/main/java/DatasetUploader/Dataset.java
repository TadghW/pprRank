package main.java.DatasetUploader;

import java.util.ArrayList;
import java.lang.Math;
import org.apache.commons.math3.stat.regression.SimpleRegression;


public class Dataset {

    private String name;
    private ArrayList<Double> magnitudes;
    private Double ppr;

    public Dataset(String name, ArrayList<Double> magnitudes){
        this.name = name;
        this.magnitudes = magnitudes;
    }

    public String getName() {
        return name;
    }
    public ArrayList<Double> getMagnitudes() {
        return magnitudes;
    }

    public Double getPpr() {
        return ppr;
    }

    public void calculatePpr(){

        //-----------------------CALCULATE PPR FROM RELATIVE MAGNITUDE | CONTEXT-----------------

        //Here comes the nerdy bit, bare with me and read see the introductory readme at the root of this repo to read about the science behind
        //purpose of the prefered preference rating. 

        //First let's define our anatomical transfer function. ATFs (aka HRTFs) are essentially models of how the sound recieved at the eardrum of 
        //the user of a supra/circum aural audio device will have been transformed by the space between the ear drum and device transducer.

        //A headphone tuned to sound like a speaker at a distance would sound terrible connected to your head because of the changes in acoustic
        //environment created by attaching it to your head. Therefore, if we can figure out what changes a signal undergoes before reaching the 
        //eardrum of an average person we can make a headphone that accounts for them in its design and sounds undistorted.

        //Unbelievably clever people like Dr.Sean Olive and Dr.Floyd Toole have refined these transfer functions over time through a mixture of 
        //theoretical and experimental data. Harman's AE/OE target is the culmination of their work. 

        //Olive's Predicted Preference Rating calculation looks at measurements of a headphone's output and compares data derived from that measurement
        //to the Harman ATF to derive a prediction of how natural a headphone will sound to an average listener. In order to perform this calculation 
        //we first need two pieces of information.

        //The first is our list of preferred frequencies. When we analyse an acoustic signal we're usually talking about how a given sound source produces
        //some frequencies of noises with more emphasis, or more quietly, than others. Imagine a performance with an oboe and a picollo. In person both
        //instruments sound harmoniously balanced. Even if a perfect recording of this performance was made, if it was played back through a poorly designed
        //speaker or headphone, the oboe may eclipse the picollo due to the speaker or headphone producing much louder lower frequency (deeper) noises than
        //higher frequency (tinnier) noises. Another poorly designed device might do the opposite. 
        
        //Audio measurements operate by producing all frequencies of sound at equal volumes at different times, measuring how loud the output is, and comparing
        //the magnitude of output at each frequency to the others. Using that data we can see clearly where that variety of distortion is. 
        
        
        //--------------------------------------IMPORTANT DATA-------------------------------------
        
        //We only need to measure some of the frequencies to ensure that we have a good sample of data, but for our measurements and analysis to work correctly 
        //we need to have an agreed upon set of frequencies to consider. So first we need to declare a list of preferred frequencies.  

        double[] preferredFrequencies = {20,21.2,22.4,23.6,25,26.5,28,30,31.5,33.5,35.5,37.5,40,42.5,45,47.5,50,53,56,60,63,67,71,75,80,85,90,95,100,106,112,118,125,132,140,150,
            160,170,180,190,200,212,224,236,250,265,280,300,315,335,355,375,400,425,450,475,500,530,560,600,630,670,710,750,800,850,900,950,1000,1060,1120,1180,
            1250,1320,1400,1500,1600,1700,1800,1900,2000,2120,2240,2360,2500,2650,2800,3000,3150,3350,3550,3750,4000,4250,4500,4750,5000,5300,5600,6000,6300,
            6700,7100,7500,8000,8500,9000,9500,10000,10600,11200,11800,12500,13200,14000,15000,16000,17000,18000,19000,20000};
        
        //Now we can declare what the ideal magnitude for each frequency should be

        double[] harmanAeOe2018 = {4.7,4.84,4.88,4.87,4.86,4.85,4.83,4.8,4.76,4.72,4.66,4.61,4.54,4.47,4.38,4.28,4.16,4.03,3.89,3.74,3.58,3.41,3.23,3.04,
            2.83,2.61,2.38,2.14,1.89,1.61,1.33,1.04,0.75,0.47,0.2,-0.06,-0.32,-0.55,-0.77,-0.96,-1.11,-1.21,-1.25,-1.24,-1.18,-1.08,-0.96,-0.83,-0.7,
            -0.57,-0.46,-0.37,-0.29,-0.23,-0.16,-0.08,0,0.1,0.2,0.3,0.39,0.48,0.55,0.61,0.66,0.69,0.73,0.77,0.84,0.96,1.13,1.36,1.66,2.01,2.44,2.94,3.51,
            4.13,4.79,5.45,6.11,6.73,7.3,7.81,8.25,8.62,8.92,9.16,9.33,9.42,9.41,9.28,9.03,8.68,8.24,7.77,7.29,6.83,6.4,5.97,5.53,5.05,4.52,3.92,3.27,2.58,
            1.84,1.04,0.14,-0.85,-1.93,-3.07,-4.18,-5.22,-6.16,-7.11,-8.3,-10.07,-12.8,-16.83,-22.32};

        //For one of our equation's variables we need to provide the frequencies as co-ordinates on an x-axis. Because human hearing percieves frequencies
        //logarithmically our preferred frequencies proceeed exponentially. We can lay out that exponential progress as the linear co-ordinates the regression
        //method accepts and it'll work just fine. I've done this in advance so we don't need to redo that calculation everytime this method is called.

        double[] preferredFrequenciesLin = {2.995732274,3.054001182,3.109060959,3.161246712,3.218875825,3.277144733,3.33220451,3.401197382,3.449987546,3.511545439,
            3.569532696,3.624340933,3.688879454,3.749504076,3.80666249,3.860729711,3.912023005,3.970291914,4.025351691,4.094344562,4.143134726,4.204692619,4.262679877,
            4.317488114,4.382026635,4.442651256,4.49980967,4.553876892,4.605170186,4.663439094,4.718498871,4.770684624,4.828313737,4.882801923,4.941642423,5.010635294,
            5.075173815,5.135798437,5.192956851,5.247024072,5.298317367,5.356586275,5.411646052,5.463831805,5.521460918,5.579729826,5.634789603,5.703782475,5.752572639,
            5.814130532,5.872117789,5.926926026,5.991464547,6.052089169,6.109247583,6.163314804,6.214608098,6.272877007,6.327936784,6.396929655,6.445719819,6.507277712,
            6.56526497,6.620073207,6.684611728,6.745236349,6.802394763,6.856461985,6.907755279,6.966024187,7.021083964,7.073269717,7.13089883,7.185387016,7.244227516,
            7.313220387,7.377758908,7.43838353,7.495541944,7.549609165,7.60090246,7.659171368,7.714231145,7.766416898,7.824046011,7.882314919,7.937374696,8.006367568,
            8.055157732,8.116715625,8.174702882,8.229511119,8.29404964,8.354674262,8.411832676,8.465899897,8.517193191,8.5754621,8.630521877,8.699514748,8.748304912,
            8.809862805,8.867850063,8.9226583,8.987196821,9.047821442,9.104979856,9.159047078,9.210340372,9.26860928,9.323669057,9.37585481,9.433483923,9.487972109,
            9.546812609,9.61580548,9.680344001,9.740968623,9.798127037,9.852194258,9.903487553};

        //---------------------FINDING THE VARIABLES | STANDARD DEVIATION OF SAMPLE---------------- 
        
        //Calculating PPR isn't all that complicated in and of itself, but we do need to calculate two important variables before we can begin. The first of these
        //is essentially a refined measure of how much the samples in the dataset deviate from Harman's AE OE target. To get standard deviation you need to find
        //the mean deviation, find each sample's deviation from that mean, square them, sum the squares, divide them by the sample size, and then find the square 
        //root of the remaining figure. 
        
        //Step 1: Find mean deviation

        ArrayList<Double> deviations  = new ArrayList<Double>();
        double totalDeviation = 0;
            
        for(int i = 16; i < 108; i++){
            double magnitude = this.magnitudes.get(i);
            double target = harmanAeOe2018[i];
            double deviation = magnitude - harmanAeOe2018[i];
            deviations.add(deviation);
            totalDeviation += deviation;
        }

        double meanDeviation = totalDeviation / deviations.size();

        //Step 2: Find each magnitude's deviation from the mean deviation
        ArrayList<Double> deviationsFromMean = new ArrayList<Double>();

        for(int i = 16; i < 108; i++){
            Double magnitude = this.magnitudes.get(i);
            Double deviationFromMean = magnitude - meanDeviation;
            deviationsFromMean.add(deviationFromMean);
        }

        //Step 3: Square each deviation from the mean and sum them
        double sumOfDeviationsFromMeanSquared = 0;

        for(double deviationFromMean : deviationsFromMean){
            double squaredDeviationMinusMean = deviationFromMean * deviationFromMean;
            sumOfDeviationsFromMeanSquared += squaredDeviationMinusMean;
        }

        //Step 4: Find variance
        double variance = sumOfDeviationsFromMeanSquared / 93;

        //Step 5: Find square root of the variance
        double standardDeviation = Math.sqrt(variance);

        
        //---------------------FINDING THE VARIABLES | SLOPE OF LINEAR REGRESSION OF MAGNITUDES---------------- 

        //Ok, now that we have our standard deviation calculated we need to find the slope of the linear equation that describes
        //the curve of our dataset's deviancy from our ATF. I use Apache Commons Math 4's statistics package here because it's 
        //very readable

        SimpleRegression regression = new SimpleRegression();
            
        //We have to load our data into the regression before slope can be estimated
        for(int i = 16; i < 108; i++){
            regression.addData(preferredFrequenciesLin[i], this.magnitudes.get(i));
        }

        //Now we can calculate our slope
        Double slope = regression.getSlope();

        //Technically we need the absolute value of the slope so
        Double absSlope = Math.abs(slope);

        //Now that we have our two variables, calculating PPR is simple
        Double ppr = 114.490443008238 - (12.6217151040598 * standardDeviation) - (15.5163857197367 * absSlope);

        //Now we know our dataset's ppr!
        this.ppr = ppr;
    }


}