
package fi.tuni.valuuttamuunnin;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

// Luokka valuuttojen tietojen hakemiselle
public class ValuuttaAPI {
    private ArrayList<Valuutta> valuutat;
    private static HttpURLConnection conn;
    private static final String CURRENCY = "currency";
    private static final String RATE = "rate";

    
    public ValuuttaAPI() {
        valuutat = new ArrayList<>();
        requestValuutta();
    }
    
    public ArrayList<Valuutta> getValuutat() {
        return valuutat;
    }
    
    // Haetaan valuuttojen tiedot sivustolta
    private void requestValuutta() {
        try {
            URL url = new URL("https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml");
            conn = (HttpURLConnection) url.openConnection();
            
            conn.setRequestMethod("GET");
            int status = conn.getResponseCode();
            
            // Varmistetaan, että avaaminen onnistui
            if (status < 300) {
                // Haetaan valuuttanodet ja käydään ne läpi
                Document doc = parseXML(conn.getInputStream());
                NodeList nodes = doc.getElementsByTagName("Cube");
                
                for (int i = 0; i < nodes.getLength(); i++) {
                    NamedNodeMap attribs = nodes.item(i).getAttributes();
                    if (attribs.getLength() > 0) {
                        Node node = attribs.getNamedItem(CURRENCY);
                        if (node != null) {
                            String nimi = node.getNodeValue();
                            double arvo = Double.parseDouble
                                    (attribs.getNamedItem(RATE).getNodeValue());
                            Valuutta uusiArvo = new Valuutta(nimi, arvo);
                            valuutat.add(uusiArvo);
                        }  
                    }
                 }
            }
        } catch (MalformedURLException e) {
            System.out.println("Virhe: " + e);
        } catch (IOException e) { 
            System.out.println("Virhe: " + e);
        } finally {
            conn.disconnect();
        }
    }
   
    private Document parseXML(InputStream stream) {
        DocumentBuilderFactory builderFactory = null;
        DocumentBuilder documentBuilder = null;
        Document doc = null;
        
        try {
            builderFactory = DocumentBuilderFactory.newInstance();
            documentBuilder = builderFactory.newDocumentBuilder();
            doc = documentBuilder.parse(stream);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            System.out.println("Parsevirhe: " + e);
        }
        
        return doc;
    }
}
