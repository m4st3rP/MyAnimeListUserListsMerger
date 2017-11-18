package xmlparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLParser {

    private int maxCount = 1;
    private int userCounter = 0;

    public static void main(String[] args) {
        XMLParser myInstance = new XMLParser();
        try {
            myInstance.parseXML();
        } catch (IOException | SAXException | ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    private void parseXML() throws IOException, ParserConfigurationException, SAXException {
        String readLine;
        String newReadLine = "";
        BufferedReader reader = new BufferedReader(new FileReader("malusers.txt"));
        ArrayList<HashMap<String, Row>> listOfMaps = new ArrayList<>();
        while ((readLine = reader.readLine()) != null) {
            int lastSlashPosition = readLine.lastIndexOf("/");
            if (lastSlashPosition != -1) {
                for (int i = lastSlashPosition + 1; i < readLine.length(); i++) {
                    newReadLine += readLine.charAt(i);
                }
                readLine = newReadLine;
                newReadLine = "";
            }
            HashMap<String, Row> aMap = getAndParseXmlForUser(readLine);
            listOfMaps.add(aMap);
        }

        HashMap<String, Row> combindedMap = listOfMaps.stream().reduce(new HashMap<>(), this::mergeMaps);

        reader.close();

        try {
            writeCSV(combindedMap);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private HashMap<String, Row> getAndParseXmlForUser(String user) throws IOException, ParserConfigurationException, SAXException {
        String myAnimeListUserURL = "https://myanimelist.net/malappinfo.php?u=" + user + "&status=all&type=anime";
        File xmlFile = new File("animelist", "tmp");
        URL url = new URL(myAnimeListUserURL);

         //sleep is necessary because the MAL API complains at too many requests
         //skip for first user
        if (userCounter != 0) {
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        userCounter++;
        System.out.println("Processing User #" + userCounter + ": " + user);
        FileUtils.copyURLToFile(url, xmlFile);

        // Create the hashmap for this user
        HashMap<String, Row> animeMap = new HashMap<>();

        // Parse the xml
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(xmlFile);
        doc.getDocumentElement().normalize();
        NodeList nList = doc.getElementsByTagName("anime");

        // go through the parsed list and add it to the users list
        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);

            // if its a valid anime node, create a row, extract the data and save it into the user
            // animelist
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element elem = (Element) nNode;
                // only add anime if it is not PTW
                if (Integer.parseInt(elem.getElementsByTagName("my_status").item(0).getTextContent()) != 6) {
                    Row row = new Row();
                    row.name = elem.getElementsByTagName("series_title").item(0).getTextContent();
                    row.count = 1;
                    // only add score and scorecount if score is not 0
                    if (Integer.parseInt(elem.getElementsByTagName("my_score").item(0).getTextContent()) == 0) {
                        row.scoreCount = 0;
                        row.score = 0;
                    } else {
                        row.score = Double.parseDouble(elem.getElementsByTagName("my_score").item(0).getTextContent());
                        row.scoreCount = 1;
                    }

                    String key = elem.getElementsByTagName("series_animedb_id").item(0).getTextContent();
                    // put the anime into the row
                    animeMap.put(key, row);
                }
            }
        }
        return animeMap;
    }

    private HashMap<String, Row> mergeMaps(HashMap<String, Row> mapA, HashMap<String, Row> mapB) {
        HashMap<String, Row> newMap = new HashMap<>();
        newMap.putAll(mapA);

        for (Map.Entry<String, Row> entry : mapB.entrySet()) {
            if (newMap.containsKey(entry.getKey())) { // If the key is already in the map, we have a duplicate!

                // We need to update the existing entry

                // Only proceed if the user gave a rating
                if (entry.getValue().score != 0) {
                    if (newMap.get(entry.getKey()).score > 0) {
                        newMap.get(entry.getKey()).score = ((newMap.get(entry.getKey()).score * newMap.get(entry.getKey()).scoreCount) + entry.getValue().score) / (newMap.get(entry.getKey()).scoreCount + 1);
                        newMap.get(entry.getKey()).scoreCount += 1;
                    } else {
                        newMap.get(entry.getKey()).score = entry.getValue().score;
                        newMap.get(entry.getKey()).scoreCount = 1;
                    }
                }
                newMap.get(entry.getKey()).count += 1;
                if (newMap.get(entry.getKey()).count > maxCount) {
                    maxCount = newMap.get(entry.getKey()).count;
                }
                // this should be it
            } else { // Key of map 2 is not in the new map, just add the non duplicate
                newMap.put(entry.getKey(), entry.getValue());
            }
        }
        return newMap;
    }

    private void writeCSV(HashMap<String, Row> map) {
        double factorizedScore;
        double scoreCountNormalization;
        double weightedScore;
        final double FACTOR = 0.866;
        String link;

        System.out.println("Writing File");
        String result = "Name^Score^Count^Score Count^Weighted Score^Link" + "\n";

        for (Map.Entry<String, Row> entry : map.entrySet()) {
            link = "https://myanimelist.net/anime/" + entry.getKey();
            factorizedScore = (double) entry.getValue().score * FACTOR;
            scoreCountNormalization = (double) entry.getValue().count / maxCount;
            weightedScore = factorizedScore + scoreCountNormalization * (1.0 - FACTOR) * 10.0;
            result = result + entry.getValue() + "^" + weightedScore + "^" + link + "\n";
        }

        try {
            FileUtils.writeStringToFile(new File("MergedLists.txt"), result, "UTF-8");
            System.out.println("Finished!");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
