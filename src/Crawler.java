import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Crawler {
  private CrawlConfig config;
  private Queue<String> frontier;
  private HashSet<String> visited;
  private int pagesCrawled;
  PrintWriter output;
  PrintWriter docsDownload;


  /* Constructor assuming default crawl config */
  public Crawler() {
    this(new CrawlConfig());
  }


  /* Constructor accepting custom config for the crawler */
  public Crawler(CrawlConfig config) {
    this.config = config;
    frontier = new LinkedList<String>();
    visited = new HashSet<String>();
    frontier.add(config.getSeedURL());
    pagesCrawled = 0;
  }


  /**
   * Set up parameters for BreadthFirst crawl
   * 
   * @param isFocused - true if a focused crawl
   * @param shouldDownload - true if documents need to be saved
   */
  public void crawlBreadthFirst(boolean isFocused, boolean shouldDownload) {
    try {
      // initialize output-writer handles
      docsDownload = new PrintWriter(config.getdocsDownloadPath());
      output = new PrintWriter(config.getBreadthFirstOutputPath());

      // separate output handle for focused crawl
      if (isFocused)
        output = new PrintWriter(config.getFocusedCrawlOutputPath());

      // print header to output
      println(output, "Count | Text | Depth | URL");

      // initialize traversal parameters to that of the seed
      int depth = 1;
      int pageCount = 1;
      String currentURL = frontier.poll();
      frontier.add(null); // delimiting levels with null 

      // print seed parameters to output
      println(output, pageCount + " | Seed | " + depth + " | " + currentURL);

      // kick-start breadth-first traversal from seed
      pageCount =
          bfs(loadFromURL(currentURL, shouldDownload), depth, pageCount, isFocused);

      // complete downloading from pending crawls
      if (shouldDownload)
        while (pagesCrawled < pageCount) {
          String nextUrl = frontier.poll();
          if (nextUrl == null)
            continue;
          loadFromURL(nextUrl, shouldDownload);
          System.out.println(pagesCrawled + ". loaded: " + nextUrl);
        }

      // close output-writer handles
      output.close();
      docsDownload.close();

    } catch (FileNotFoundException fne) {
      fne.printStackTrace();
    }
  }


  /**
   * Performs Breadth First Traversal over a given page
   * 
   * @param page - page handle for this node
   * @param depth - depth of this node
   * @param pageCount - URLs recorded prior to call
   * @param isFocused - true if focused crawl
   * @return pageCount post traversal complete
   */
  private int bfs(Document page, int depth, int pageCount, boolean isFocused) {
    // extract unique crawlable URLs from the page
    List<String[]> urlTxtPairs = getValidURLsFromPage(page);

    // perform additional filters in case of focused crawl
    if (isFocused)
      performFocusedFilter(urlTxtPairs);

    // iterate over all URLs parsed or till expected page count reached
    for (int i = 0; i < urlTxtPairs.size() && pageCount < config.getPageCount(); i++) {
      String text = urlTxtPairs.get(i)[1];
      String url = urlTxtPairs.get(i)[0];

      // check if URL has already been registered
      if (visited.contains(url))
        continue;
      frontier.add(url);
      visited.add(url);

      // write to output
      println(output, (++pageCount) + " | " + text + " | " + depth + " | " + url);
      System.out.println((pageCount) + " | " + text + " | " + depth + " | " + url);

      // return if expected page count reached
      if (pageCount >= config.getPageCount())
        return pageCount;
    }

    // if depth has not reached maximum allowed depth
    if (depth < config.getMaxDepth()) {
      String next = frontier.poll();
      // keep track of depth of the traversal
      // null represents end of a level
      if (next == null) {
        frontier.add(null);
        depth++;
        next = frontier.poll();
        // consecutive nulls represent end of levels
        if (next == null)
          return pageCount;
      }

      // traverse recursively breadth-first
      pageCount = bfs(loadFromURL(next, !isFocused), depth, pageCount, isFocused);
    }

    // page count after this breadth-first call
    return pageCount;
  }


  /**
   * Filter URL-Text pairs for valid variations of the given keywords in the config
   * 
   * @param urlTxtPairs - URL and anchor text pair
   */
  private void performFocusedFilter(List<String[]> urlTxtPairs) {
    Iterator<String[]> pairIter = urlTxtPairs.iterator();

    while (pairIter.hasNext()) {
      String[] urlTxtPair = (String[]) pairIter.next();

      // Case Folding
      String url = urlTxtPair[0].toLowerCase();
      String text = urlTxtPair[1].toLowerCase();

      // Parsing the article name from the url
      String articleName = url.substring(url.lastIndexOf('/') + 1, url.length());

      // check if the URL or text contains valid variations of the keywords
      int unmatched = 0;
      for (String keyword : config.getFocusedCrawlKeywords())
        if (matches(keyword, articleName) || matches(keyword, text))
          break;
        else
          unmatched++;

      // when both articleName and text doesn't match with any of the keywords 
      if (unmatched == config.getFocusedCrawlKeywords().size())
        pairIter.remove();
    }
  }


  /* Check if the given text has a valid variation of the keyword */
  private boolean matches(String keyword, String text) {
    return text.contains(keyword.toLowerCase());
  }


  /* Set up parameters for DepthFirst crawl */
  public void crawlDepthFirst() {
    try {
      // initialize output-writer handle
      output = new PrintWriter(config.getDepthFirstOutputPath());
      println(output, "Count | Text | Depth | URL");

      // initialize traversal parameters to that of the seed
      int depth = 1;
      int pageCount = 1;
      String currentURL = frontier.poll();

      // print seed parameters to output
      println(output, pageCount + " | Seed | " + depth + " | " + currentURL);

      // kick-start breadth-first traversal from seed
      dfs(loadFromURL(currentURL, false), depth, pageCount);

      // close output-writer handle
      output.close();
    } catch (FileNotFoundException fne) {
      fne.printStackTrace();
    }
  }


  /**
   * Performs Depth First Traversal over a given page
   * 
   * @param page - page handle for this node
   * @param depth - depth of this node
   * @param pageCount - URLs recorded prior to this call
   * @return pageCount post traversal complete
   */
  private int dfs(Document page, int depth, int pageCount) {
    // extract unique crawlable URLs from the page
    List<String[]> urlTxtPairs = getValidURLsFromPage(page);

    // iterate over all URLs parsed or till expected page count reached
    for (int i = 0; i < urlTxtPairs.size() && pageCount < config.getPageCount(); i++) {
      String text = urlTxtPairs.get(i)[1];
      String url = urlTxtPairs.get(i)[0];

      // check if url has already been registered
      if (visited.contains(url))
        continue;
      visited.add(url);

      // write to output
      println(output, (++pageCount) + " | " + text + " | " + depth + " | " + url);
      System.out.println((pageCount) + " | " + text + " | " + depth + " | " + url);

      // if depth has not reached maximum allowed depth and target page count not reached,
      // traverse recursively depth-first
      if (depth < config.getMaxDepth() && pageCount < config.getPageCount())
        pageCount = dfs(loadFromURL(url, false), depth + 1, pageCount);
    }
    return pageCount;
  }


  /**
   * Prints string to the given handle
   * 
   * @param output - output handle
   * @param string - output string
   */
  private void println(PrintWriter output, String string) {
    output.println(string);
    output.flush();
  }


  /**
   * Extract valid crawlable URLs from the given page
   * 
   * @param page - node to be crawled
   * @return URL-Text pairs for crawlable unique URLs. 0 - url 1 - text
   */
  private List<String[]> getValidURLsFromPage(Document page) {
    List<String[]> urlTxtPairs = new ArrayList<String[]>();
    HashSet<String> currentSet = new HashSet<String>();

    // sets the domain name as the BaseUri
    page.setBaseUri(config.getBaseUri());

    // Pre-Processing - Exclude non-content portion of the page
    for (String exclusionSelector : config.getCrawlExclusionSelectors())
      page.select(exclusionSelector).remove();
    Elements hyperLinks = page.select(config.getMainContentSelector());

    // Exclude administrative and previously visited URLs
    Iterator<Element> iter = hyperLinks.iterator();
    while (iter.hasNext()) {
      Element anchor = (Element) iter.next();
      String href = anchor.absUrl("href");
      String plainHref = anchor.attr("href");

      // remove administrative URLs and other irrelevant redirections
      boolean removeCondition = !href.contains(config.getBaseUri());
      removeCondition = removeCondition || !plainHref.startsWith(config.getArticleType());
      removeCondition = removeCondition || href.contains("#");
      removeCondition = removeCondition || plainHref.contains(":");

      // exclude pages already visited
      if (removeCondition || visited.contains(href))
        iter.remove();

      // form a URL-Text pair
      else if (!currentSet.contains(href)) {
        String[] urlTxtPair = new String[2];
        urlTxtPair[0] = href;
        urlTxtPair[1] = anchor.text().trim();
        urlTxtPairs.add(urlTxtPair);
        currentSet.add(href);
      }
    }
    return urlTxtPairs;
  }


  /**
   * @return a document obtained post establishing an HttpRequest to the given URL
   * @param url
   * @param shouldDownload - flag whether the document should be saved
   * 
   */
  private Document loadFromURL(String url, boolean shouldDownload) {
    try {
      // politeness wait
      Thread.sleep(config.getPolitenessWait());

      // connecting to the URL, with a timeout of 20 seconds
      Document page = Jsoup.connect(url).timeout(20000).get();
      pagesCrawled++;

      // save documents if asked to
      if (shouldDownload)
        storeDocTrecFormat(url, page);

      return page;
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException ie) {
      ie.printStackTrace();
    }
    return null;
  }


  /* Stores document in TREC recommended format using the available output handle */
  private void storeDocTrecFormat(String url, Document page) {
    LocalTime currentTime = LocalTime.now();
    LocalDate currentDate = LocalDate.now();
    String pageHtml = page.outerHtml();

    println(docsDownload, "<DOC>");
    println(docsDownload, "<DOCNO>WTX-" + currentTime + "</DOCNO>");
    println(docsDownload, "<DOCHDR>");
    println(docsDownload, url);
    println(docsDownload, "Date: " + currentDate);
    println(docsDownload, "Content-type: text/html");
    println(docsDownload, "Content-length: " + String.valueOf(pageHtml.length()));
    println(docsDownload, "Last-modified: " + currentDate);
    println(docsDownload, "</DOCHDR>");
    println(docsDownload, pageHtml);
    println(docsDownload, "</DOC>");
  }


}
