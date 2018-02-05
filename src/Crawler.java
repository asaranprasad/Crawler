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
  PrintWriter output;
  PrintWriter docsDownload;
  private HashSet<String> visited;


  public Crawler() {
    this(new CrawlConfig());
  }


  public Crawler(CrawlConfig config) {
    this.config = config;
    frontier = new LinkedList<String>();
    visited = new HashSet<String>();
    frontier.add(config.getSeedURL());
  }


  public void crawlBreadthFirst(boolean isFocused) throws FileNotFoundException {
    docsDownload = new PrintWriter(config.getdocsDownloadPath());
    output = new PrintWriter(config.getOutputFolderPath() + "crawlBreadthFirst.txt");
    boolean shouldDownload = true;
    if (isFocused) {
      output = new PrintWriter(config.getOutputFolderPath() + "crawlFocused.txt");
      shouldDownload = false;
    }

    println(output, "Count | Text | Depth | URL");

    int depth = 1;
    int pageCount = 1;
    String currentURL = frontier.poll();
    frontier.add(null);

    println(output, pageCount + " | Seed | " + depth + " | " + currentURL);

    pageCount = bfs(loadFromURL(currentURL, shouldDownload), depth, pageCount, isFocused);
    output.close();


    // complete downloading from pending crawls
    while (visited.size() < pageCount) {
      String nextUrl = frontier.poll();
      if (nextUrl == null)
        continue;
      loadFromURL(nextUrl, shouldDownload);
      System.out.println(visited.size() + ". loaded: " + nextUrl);
    }
    docsDownload.close();
  }


  private int bfs(Document page, int depth, int pageCount, boolean isFocused) {
    List<String[]> urlTxtPairs = getValidURLsFromPage(page);

    if (isFocused)
      performFocusedFilter(urlTxtPairs);

    for (int i = 0; i < urlTxtPairs.size() && pageCount < config.getPageCount(); i++) {
      String text = urlTxtPairs.get(i)[1];
      String url = urlTxtPairs.get(i)[0];
      frontier.add(url);

      println(output, (++pageCount) + " | " + text + " | " + depth + " | " + url);
      System.out.println((pageCount) + " | " + text + " | " + depth + " | " + url);

      if (pageCount >= config.getPageCount())
        return pageCount;
    }

    if (depth < 6) {
      String next = frontier.poll();
      if (next == null) {
        frontier.add(null);
        depth++;
        next = frontier.poll();
        if (next == null)
          return pageCount;
      }
      pageCount = bfs(loadFromURL(next, !isFocused), depth, pageCount, isFocused);
    }
    return pageCount;
  }


  private void performFocusedFilter(List<String[]> urlTxtPairs) {
    Iterator<String[]> pairIter = urlTxtPairs.iterator();
    while (pairIter.hasNext()) {
      String[] urlTxtPair = (String[]) pairIter.next();
      // Case Folding
      String url = urlTxtPair[0].toLowerCase();
      String text = urlTxtPair[1].toLowerCase();

      // Parsing the article name from the url
      String articleName = url.substring(url.lastIndexOf('/') + 1, url.length());

      // if both articleName and text doesn't match with any of the keywords 
      int unmatched = 0;
      for (String keyword : config.getFocusedCrawlKeywords())
        if (matches(keyword, articleName) || matches(keyword, text))
          break;
        else
          unmatched++;

      if (unmatched == config.getFocusedCrawlKeywords().size())
        pairIter.remove();
    }
  }


  private boolean matches(String keyword, String text) {
    return text.contains(keyword.toLowerCase());
  }


  public void crawlDepthFirst() throws FileNotFoundException {
    output = new PrintWriter(config.getOutputFolderPath() + "crawlDepthFirst.txt");
    println(output, "Count | Text | Depth | URL");

    int depth = 1;
    int pageCount = 1;
    String currentURL = frontier.poll();

    println(output, pageCount + " | Seed | " + depth + " | " + currentURL);

    dfs(loadFromURL(currentURL, false), depth, pageCount);
    output.close();
  }


  private int dfs(Document page, int depth, int pageCount) {
    List<String[]> urlTxtPairs = getValidURLsFromPage(page);

    for (int i = 0; i < urlTxtPairs.size() && pageCount < config.getPageCount(); i++) {
      String text = urlTxtPairs.get(i)[1];
      String url = urlTxtPairs.get(i)[0];
      println(output, (++pageCount) + " | " + text + " | " + depth + " | " + url);

      System.out.println((pageCount) + " | " + text + " | " + depth + " | " + url);

      if (pageCount >= config.getPageCount())
        break;

      if (depth < 6)
        pageCount = dfs(loadFromURL(url, false), depth + 1, pageCount);
    }
    return pageCount;
  }

  private void println(PrintWriter output, String string) {
    output.println(string);
    output.flush();
  }


  private List<String[]> getValidURLsFromPage(Document page) {
    List<String[]> urlTxtPairs = new ArrayList<String[]>();
    HashSet<String> currentSet = new HashSet<String>();

    page.setBaseUri(config.getBaseUri());

    // Preprocessing
    // Including only the content portion of the page
    page.select("[role=navigation]").remove();
    page.select("[class='external text']").remove();
    page.select("[class*='navigation']").remove();
    Elements hyperLinks = page.select("#mw-content-text a");

    // Exclude urls out of the current domain
    Iterator<Element> iter = hyperLinks.iterator();
    while (iter.hasNext()) {
      Element anchor = (Element) iter.next();
      String href = anchor.absUrl("href");
      String plainHref = anchor.attr("href");
      String anchorClass = anchor.attr("class");


      // remove images and other irrelevant redirections
      boolean removeCondition = !href.contains("en.wikipedia.org");
      removeCondition = removeCondition || anchorClass.equals("image");
      removeCondition = removeCondition || anchorClass.equals("internal");
      removeCondition = removeCondition || anchorClass.equals("mw-wiki-logo");
      removeCondition = removeCondition || href.contains("#");
      removeCondition = removeCondition || plainHref.contains(":");
      removeCondition = removeCondition || !plainHref.startsWith("/wiki/");

      // exclude pages already visited
      if (removeCondition || visited.contains(href))
        iter.remove();

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


  private Document loadFromURL(String url, boolean shouldDownload) {
    try {
      Thread.sleep(config.getPolitenessWait());

      // check if URL not already visited
      if (visited.contains(url))
        return null;

      Document page = Jsoup.connect(url).timeout(10000).get();
      visited.add(url);

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
