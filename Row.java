package xmlparser;

public class Row {
  public String name;
  public double score;
  public int count;
  public int scoreCount;
  public double weightedScore;
  public String link = "https://myanimelist.net/anime/";


  @Override
  public String toString() {
    return name + "^" + score + "^" + count + "^" + scoreCount + "^" + weightedScore + "^" + link;
  }
}
