package parseAndProcess;

public class Row {
  public String name;
  public double score;
  public int count;
  public int scoreCount;


  @Override
  public String toString() {
    return name + "^" + score + "^" + count + "^" + scoreCount;
  }
}
