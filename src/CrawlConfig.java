import java.util.ArrayList;
import java.util.List;

public class CrawlConfig {

  private String seedURL;
  private String baseUri;
  private int maxDepth;
  private int pageCount;
  private String outputFolderPath;
  private String docsDownloadPath;
  private int politenessWait;
  private List<String> focusedCrawlKeywords;

  CrawlConfig() {
    seedURL = "https://en.wikipedia.org/wiki/Solar_eclipse";
    baseUri = "https://en.wikipedia.org";
    maxDepth = 6;
    pageCount = 1000;
    outputFolderPath = "./output/";
    docsDownloadPath = outputFolderPath + "BFSCrawledDocuments.txt";
    politenessWait = 1000;
    focusedCrawlKeywords = new ArrayList<String>();
    focusedCrawlKeywords.add("lunar");
    focusedCrawlKeywords.add("moon");
  }

  public CrawlConfig(String newSeedURL, int newMaxDepth, int newPageCount,
      String[] newFocusedCrawlKeywords) {
    seedURL = newSeedURL;
    maxDepth = newMaxDepth;
    pageCount = newPageCount;
    focusedCrawlKeywords = new ArrayList<String>();
    for (String eachKeyword : newFocusedCrawlKeywords)
      focusedCrawlKeywords.add(eachKeyword.trim().toLowerCase());
  }

  public void printConfig() {
    System.out.println("seedURL: " + seedURL);
    System.out.println("maxDepth: " + maxDepth);
    System.out.println("pageCount: " + pageCount);
    System.out.println("Focused Crawl Keywords:");
    for (String eachKeyword : focusedCrawlKeywords)
      System.out.println(eachKeyword);
  }

  public String getSeedURL() {
    return seedURL;
  }

  public int getMaxDepth() {
    return maxDepth;
  }

  public int getPageCount() {
    return pageCount;
  }

  public String getOutputFolderPath() {
    return outputFolderPath;
  }

  public String getdocsDownloadPath() {
    return docsDownloadPath;
  }

  public int getPolitenessWait() {
    return politenessWait;
  }

  public String getBaseUri() {
    return baseUri;
  }

  public List<String> getFocusedCrawlKeywords() {
    return focusedCrawlKeywords;
  }

}
