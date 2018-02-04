import java.util.ArrayList;
import java.util.List;

public class CrawlConfig {

  private String seedURL;
  private int maxDepth;
  private List<String> focusedCrawlKeywords;

  CrawlConfig() {
    seedURL = "https://en.wikipedia.org/wiki/Solar_eclipse";
    maxDepth = 6;
    focusedCrawlKeywords = new ArrayList<String>();
    focusedCrawlKeywords.add("lunar");
    focusedCrawlKeywords.add("moon");
  }

  public CrawlConfig(String newSeedURL, int newMaxDepth,
      String[] newFocusedCrawlKeywords) {
    seedURL = newSeedURL;
    maxDepth = newMaxDepth;
    focusedCrawlKeywords = new ArrayList<String>();
    for (String eachKeyword : newFocusedCrawlKeywords)
      focusedCrawlKeywords.add(eachKeyword.trim().toLowerCase());
  }


  public void printConfig() {
    System.out.println("seedURL: " + seedURL);
    System.out.println("maxDepth: " + maxDepth);
    System.out.println("Focused Crawl Keywords:");
    for (String eachKeyword : focusedCrawlKeywords)
      System.out.println(eachKeyword);
  }
}
