package merger;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Merger {
    // Attributes
    private final int sleepTime;
    private final Map<Integer, Row> mergedAnimeListsMap;
    private final Set<Integer> ignoredAnimeSet;
    private final Set<String> ignoredUsers;
    private final Set<String> users;
    private double averageScore;
    private int maxCount;

    // Constructor(s)
    public Merger(int sleepTime, String users, String ignoredUsers) {
        this.sleepTime = sleepTime;
        mergedAnimeListsMap = new HashMap<>();
        ignoredAnimeSet = new HashSet<>();
        this.users = reduceSetStingsToUserName(new HashSet<>(Arrays.asList(users.split("\\r?\\n"))));
        this.ignoredUsers = reduceSetStingsToUserName(new HashSet<>(Arrays.asList(ignoredUsers.split("\\r?\\n"))));
    }

    // Methods
    public void fillSetOfIgnoredAnime(String user) throws InterruptedException, IOException, SAXException, ParserConfigurationException {
        // sleep is necessary because the MAL API complains at too many requests
        TimeUnit.MILLISECONDS.sleep(sleepTime);

        URL url = new URL("https://myanimelist.net/malappinfo.php?u=" + user + "&status=all&type=anime");
        File xmlFile = new File("animelist", "tmp");
        FileUtils.copyURLToFile(url, xmlFile);

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
                // only add anime if it is completed
                if (Integer.parseInt(elem.getElementsByTagName("my_status").item(0).getTextContent()) == 2) {
                    Integer animeID = Integer.parseInt(elem.getElementsByTagName("series_animedb_id").item(0).getTextContent());
                    // put the anime into the row
                    ignoredAnimeSet.add(animeID);
                }
            }
        }
    }


    public Map<Integer, Row> createMapOfAnime(String user) throws IOException, InterruptedException, SAXException, ParserConfigurationException {
        // sleep is necessary because the MAL API complains at too many requests
        TimeUnit.MILLISECONDS.sleep(sleepTime);

        Map<Integer, Row> userMap = new HashMap<>();
        URL url = new URL("https://myanimelist.net/malappinfo.php?u=" + user + "&status=all&type=anime");
        File xmlFile = new File("animelist", "tmp");
        FileUtils.copyURLToFile(url, xmlFile);

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
                        row.score = 0;
                        row.scoreCount = 0;
                    } else {
                        row.score = Double.parseDouble(elem.getElementsByTagName("my_score").item(0).getTextContent());
                        row.scoreCount = 1;
                    }

                    Integer key = Integer.parseInt(elem.getElementsByTagName("series_animedb_id").item(0).getTextContent());
                    // put the anime into the row
                    userMap.put(key, row);
                }
            }
        }
        return userMap;
    }

    public void addMapToMergedAnimeListsMap(Map<Integer, Row> map) {
        for (Map.Entry<Integer, Row> entry : map.entrySet()) {
            if (mergedAnimeListsMap.containsKey(entry.getKey())) { // If the key is already in the map, we have a duplicate!

                // We need to update the existing entry

                // Only proceed if the user gave a rating
                if (entry.getValue().score != 0) {
                    if (mergedAnimeListsMap.get(entry.getKey()).score > 0) {
                        mergedAnimeListsMap.get(entry.getKey()).score = ((mergedAnimeListsMap.get(entry.getKey()).score * mergedAnimeListsMap.get(entry.getKey()).scoreCount) + entry.getValue().score) / (mergedAnimeListsMap.get(entry.getKey()).scoreCount + 1);
                        mergedAnimeListsMap.get(entry.getKey()).scoreCount += 1;
                    } else {
                        mergedAnimeListsMap.get(entry.getKey()).score = entry.getValue().score;
                        mergedAnimeListsMap.get(entry.getKey()).scoreCount = 1;
                    }
                }
                mergedAnimeListsMap.get(entry.getKey()).count += 1;
                // this should be it
            } else { // Key of map 2 is not in the new map, just add the non duplicate
                mergedAnimeListsMap.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public void writeCSV() {
        double factorizedScore;
        double scoreCountNormalization;
        double weightedScore;
        final double FACTOR = 0.866;
        String link;
        int maxCount = 1;
        int titleAmountWithScore = 0;
        double totalScore = 0;

        for (Map.Entry<Integer, Row> entry : mergedAnimeListsMap.entrySet()) {
            if (entry.getValue().count > maxCount) {
                maxCount = entry.getValue().count;
            }
            if (entry.getValue().score > 0) {
                titleAmountWithScore++;
            }
            totalScore += entry.getValue().score;
        }

        StringBuilder resultBuilder = new StringBuilder("Name^Score^Count^Score Count^Weighted Score^Link\n");

        for (Map.Entry<Integer, Row> entry : mergedAnimeListsMap.entrySet()) {
            link = "https://myanimelist.net/anime/" + entry.getKey();
            factorizedScore = entry.getValue().score * FACTOR;
            scoreCountNormalization = (double) entry.getValue().count / maxCount;
            weightedScore = factorizedScore + scoreCountNormalization * (1.0 - FACTOR) * 10.0;
            resultBuilder.append(entry.getValue()).append("^").append(weightedScore).append("^").append(link).append("\n");
        }
        String result = resultBuilder.toString();

        try {
            FileUtils.writeStringToFile(new File("MergedLists.txt"), result, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.maxCount = maxCount;
        averageScore = totalScore / titleAmountWithScore;
    }

    private Set<String> reduceSetStingsToUserName(Set<String> set) {
        Set<String> newSet = new HashSet<>();
        for (String s : set) {
            int lastSlashPosition = s.lastIndexOf("/");
            if (lastSlashPosition > -1) {
                newSet.add(s.substring(lastSlashPosition + 1));
            } else {
                newSet.add(s);
            }
        }
        return newSet;
    }

    public void removeAnimeFromMergedAnimeListsMap(Integer animeID) {
        if (mergedAnimeListsMap.containsKey(animeID)) mergedAnimeListsMap.remove(animeID);
    }

    public double getAverageScore() {
        return averageScore;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public Set<String> getUsers() {
        return users;
    }

    public Set<String> getIgnoredUsers() {
        return ignoredUsers;
    }

    public Set<Integer> getIgnoredAnimeSet() {
        return ignoredAnimeSet;
    }
}
