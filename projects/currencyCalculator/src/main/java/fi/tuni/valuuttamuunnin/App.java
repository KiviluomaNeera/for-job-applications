/*
Tekijä: Neera Kiviluoma
Email: neera.kiviluoma@tuni.fi

Kuvaus:
Valuuttamuunnin-sovellus, jolla käyttäjä pystyy muuntaa haluamansa summan
toiselle valuutalle. Molemmat valuutat saa valita. Laske-nappia painamalla 
tulos näytetään käyttäjälle, jos annettu arvo on sopiva ja molemmat valuutat on
valittuna. Tällöin myös edellinen tulos katoaa, jos uusi arvo ei ole sopiva.
Sovelluksen voi sulkea sulje-nappia painamalla.

Tiedot haetaan osoitteesta 
https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml
*/

package fi.tuni.valuuttamuunnin;

import java.util.ArrayList;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;


// App-luokka, joka toteuttaa käyttöliittymän ja sen toiminnallisuudet
public class App extends Application {
    private ArrayList<Valuutta> valuutat = new ArrayList<>();
    private ValuuttaAPI api;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Valuuttamuunnin");
        
        // Valuuttatietojen haku APIsta
        api = new ValuuttaAPI();
        valuutat = api.getValuutat();
        
        // Käyttöliittymäkomponenttien luominen
        GridPane grid = new GridPane();
        Scene scene = new Scene(grid, 360, 150);
        Button laskeButton = new Button("Laske");
        Button suljeButton = new Button("Sulje");
        Label ohjeLabel = new Label("Valitse summa ja käytettävät valuutat:");
        Label tulostekstiLabel = new Label("Tulos:");
        Label tulosLabel = new Label("");
        Label valiLabel = new Label("");
        Label nuoliLabel = new Label("->");
        TextField summaField = new TextField("");
        ComboBox muutettavaBox = new ComboBox();
        ComboBox muutetaanBox = new ComboBox();
        
        // Valuuttojen lyhenteiden lisääminen ComboBoxeihin.
        // EUR lisätään erikseen, sillä tiedosto esittää valuuttojen kertoimet,
        // kun summa muunnettava valuutta on euro. Tällöin sivusto ei sisällä
        // listalla euroa.
        muutettavaBox.getItems().add("EUR");
        muutetaanBox.getItems().add("EUR");
        
        for (Valuutta valuutta : valuutat) {
            muutettavaBox.getItems().add(valuutta.getValuutta());
            muutetaanBox.getItems().add(valuutta.getValuutta());
        }
        
        // Käyttöliittymäkomponenttien asettelu
        grid.add(ohjeLabel, 0, 0, 3, 1);
        grid.add(summaField, 1, 2);
        grid.add(muutettavaBox, 2, 2);
        grid.add(nuoliLabel, 3, 2);
        grid.add(muutetaanBox, 4, 2);
        grid.add(laskeButton, 1, 3);
        grid.add(valiLabel, 1, 4);
        grid.add(tulostekstiLabel, 1, 5);
        grid.add(tulosLabel, 1, 6);
        grid.add(suljeButton, 5, 7);
        
        // Sulje-napin toiminnallisuus
        suljeButton.setOnAction(e -> {
            Platform.exit();
        });
        
        
        // Laske-napin toiminnallisuus
        laskeButton.setOnAction((event) -> {
            // Tarkistetaan, että jokin summa on syötetty ja se on numeerinen
            if (!summaField.getText().equals("") && 
                    StringUtils.isNumeric(summaField.getText())) {
                
                // Haetaan CheckBoxien arvot ja varmistetaan, että niihin on
                // valittu arvo
                String muutettava = (String) muutettavaBox.getValue();
                String muutetaan = (String) muutetaanBox.getValue();
                if (muutettava != null && muutetaan != null) {
                    double summa = Double.parseDouble(summaField.getText());
                    double tulos = 0;
                    double muutettavaKerroin = haeKerroin(muutettava);
                    double muutetaanKerroin = haeKerroin(muutetaan);
                    
                    // Jos valuutat ovat samat, palautetaan annettu summa
                    if (muutettava.equals(muutetaan)) {
                        tulos = summa;
                    } else {
                        // Jos muutettava valuutta on EUR, tarvitsee vain kertoa 
                        // summa halutun valuutan kertoimella
                        if (muutettava.equals("EUR")) {
                            tulos = summa * muutetaanKerroin;
                        
                        // Jos haluttu valuutta on EUR, tarvitsee vain jakaa
                        // summa muutettavan kertoimella
                        } else if (muutetaan.equals("EUR")) {
                            tulos = summa / muutettavaKerroin;
                        
                        // Muulloin muutetaan summa ensin euroiksi ja sitten 
                        // kerrotaan halutun valuutan kertoimella
                        } else {
                            tulos = (summa / muutettavaKerroin) * muutetaanKerroin;
                        }
                    }
                    
                    // Ilmoitetaan tulos käyttäjälle
                    String teksti = String.format("%.3f %s = %.3f %s", summa, muutettava, tulos, muutetaan);
                    tulosLabel.setText(teksti);
                } else {
                    tulosLabel.setText("");
                }
            } else {
                tulosLabel.setText("");
            }
        }); 

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
    
    // Funktio kertoimen hakuun listasta
    private double haeKerroin(String valuutta) {
        double kerroin = 0;
        
        for (Valuutta val : valuutat) {
            if (val.getValuutta().equals(valuutta)) {
                kerroin = val.getKerroin();
                break;
            }
        }
        
        return kerroin;
    }
   
}