
package fi.tuni.valuuttamuunnin;

// Luokka valuutta-olioille
public class Valuutta {
    private String valuutta;
    private double kerroin;
    
    public Valuutta(String valuutta, double kerroin) {
        this.valuutta = valuutta;
        this.kerroin = kerroin;
    }
    
    public String getValuutta() {
        return valuutta;
    }
    
    public double getKerroin() {
        return kerroin;
    }
}
