import java.util.Collections;

import src.main.java.Dataset;

public class VariantPackage {

    private Dataset l1;
    private Dataset l2;
    private Dataset l3;
    private Dataset r1;
    private Dataset r2;
    private Dataset r3;

    private Double medianPprLeft;
    private Double medianPprRight;
    private Double medianPpr;
    private Double meanAbsolutePprVariance;
    private Double pprVariationBySide;
    private Double pprVariationBySeat;
    private Double sumBassLeakage;

    public VariantPackage(Dataset l1, Dataset l2, Dataset l3, Dataset r1, Dataset r2, Dataset r3){
        this.l1 = l1;
        this.l2 = l2;
        this.l3 = l3;
        this.r1 = r1;
        this.r2 = r2;
        this.r3 = r3;
    }

    private void rateVariant(){
        double[] leftSidePpr = {this.l1.getPpr(), this.l2.getPpr(), this.l3.getPpr()};
        double[] rightSidePpr = {this.r1.getPpr(), this.r2.getPpr(), this.r3.getPpr()};
        double[] totalPpr = new double[6];

        Collections.addAll(totalPpr, leftSidePpr);
        Collections.addAll(totalPpr, rightSidePpr);

        this.medianPprLeft = leftSidePpr[1];
        this.medianPprRight = rightSidePpr[1];
        this.medianPprTotal = (totalPpr[2] + totalPpr[3]) / 2;
        
        Double sumPprVariation = 0;
        for(Double ppr : totalPpr){
            sumPprVariation += (Math.abs(this.medianPpr - ppr));
        }
        this.meanAbsolutePprVariance = sumPprVariation / 6;

        this.pprVariationBySide = Math.abs(medianPprLeft - medianPprRight);

        Double highestPprLeft = Arrays.stream(leftSidePpr).max();
        Double lowestPprLeft = Arrays.stream(leftSidePpr).min();
        Double highestPprRight = Arrays.stream(rightSidePpr).max();
        Double lowestPprRight = Arrays.stream(rightSidePpr).min();
        Double maxVarianceLeft = highestPprLeft - lowestPprLeft;
        Double maxVarianceRight = highestPprRight - lowestPprRight;

        this.pprVariationBySeat = maxVarianceLeft + maxVarianceRight / 2;
        


    }

    
}
