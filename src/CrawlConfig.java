import java.util.ArrayList;
import java.util.List;

public class CrawlConfig {

  private String seedURL;
  private String baseUri;
  private int maxDepth;
  private int pageCount;
  private String outputFolderPath;
  private String docsDownloadPath;
  private String depthFirstOutputPath;
  private String breadthFirstOutputPath;
  private String focusedCrawlOutputPath;
  private String articleType;
  private int politenessWait;
  private String crawlMainContentSelector;
  private List<String> crawlExclusionSelectors;
  private List<String> focusedCrawlKeywords;

  CrawlConfig() {
    seedURL = "https://en.wikipedia.org/wiki/Solar_eclipse";
    baseUri = "https://en.wikipedia.org";
    articleType = "/wiki/";
    maxDepth = 6;
    pageCount = 1000;
    outputFolderPath = "./output/";
    docsDownloadPath = outputFolderPath + "BFSCrawledDocuments.txt";
    breadthFirstOutputPath = getOutputFolderPath() + "crawlBreadthFirst.txt";
    depthFirstOutputPath = getOutputFolderPath() + "crawlDepthFirst.txt";
    focusedCrawlOutputPath = getOutputFolderPath() + "crawlFocused.txt";
    politenessWait = 1000;
    crawlMainContentSelector = "#mw-content-text a";
    crawlExclusionSelectors = getExclusionList();
    focusedCrawlKeywords = new ArrayList<String>();
    focusedCrawlKeywords.add("lunar");
    focusedCrawlKeywords.add("moon");
  }

  private List<String> getExclusionList() {
    List<String> exclusionCSSList = new ArrayList<String>();
    exclusionCSSList.add("[role=navigation]");
    exclusionCSSList.add("[class='external text']");
    exclusionCSSList.add("[class*='navigation']");
    exclusionCSSList.add(".internal");
    exclusionCSSList.add(".image");
    exclusionCSSList.add(".mw-wiki-logo");
    return exclusionCSSList;
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

  public List<String> getCrawlExclusionSelectors() {
    return crawlExclusionSelectors;
  }

  public String getArticleType() {
    return articleType;
  }

  public String getMainContentSelector() {
    return crawlMainContentSelector;
  }

  public List<String> getFocusedCrawlKeywords() {
    return focusedCrawlKeywords;
  }

  public String getBreadthFirstOutputPath() {
    return breadthFirstOutputPath;
  }

  public String getDepthFirstOutputPath() {
    return depthFirstOutputPath;
  }

  public String getFocusedCrawlOutputPath() {
    return focusedCrawlOutputPath;
  }

}
