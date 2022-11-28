package main.java.pprrankserver;

import org.bson.types.ObjectId;
import java.util.Objects;

public class Dataset {

    private ObjectId id;
    private String name;
    private double[] magnitudes;
    private double ppr;

    public Dataset(ObjectId id, String name, double[] magnitudes, Double ppr){
        this.id = id;
        this.name = name;
        this.magnitudes = magnitudes;
        this.ppr = ppr;
    }
    
    public ObjectId getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    public double[] getMagnitudes() {
        return magnitudes;
    }

    public double getPpr() {
        return ppr;
    }

    public void setId(ObjectId id){
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMagnitudes(double[] magnitudes) {
        this.magnitudes = magnitudes;
    }

    public void setPpr(double ppr) {
        this.ppr = ppr;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Dataset{");
        sb.append("id=").append(id);
        sb.append(", name=").append(name);
        sb.append(", frequencyResponse=").append(magnitudes);
        sb.append(", ppr=").append(ppr);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, magnitudes, ppr);
    }


}