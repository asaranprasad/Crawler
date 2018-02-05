import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
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
    println(output, "Count | Text | Depth | URL");

    int depth = 1;
    int pageCount = 1;
    String currentURL = frontier.poll();

    println(output, pageCount + " | Seed | " + depth + " | " + currentURL);

    bfs(loadFromURL(currentURL, true), depth, pageCount);
    output.close();
  }

  private int bfs(Document page, int depth, int pageCount) {
    List<String[]> urlTxtPairs = getValidURLsFromPage(page);

    for (int i = 0; i < urlTxtPairs.size() && pageCount < config.getPageCount(); i++) {
      String text = urlTxtPairs.get(i)[1];
      String url = urlTxtPairs.get(i)[0];
      frontier.add(url);

      println(output, (++pageCount) + " | " + text + " | " + depth + " | " + url);
      System.out.println((pageCount) + " | " + text + " | " + depth + " | " + url);

      if (pageCount >= config.getPageCount())
        return pageCount;
    }

    if (depth < 6)
      pageCount = bfs(loadFromURL(frontier.poll(), true), depth + 1, pageCount);

    return pageCount;
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


  private Document loadFromURL(String url, boolean shouldBackup) {
    try {
      Thread.sleep(config.getPolitenessWait());
      visited.add(url);
      Document page = Jsoup.connect(url).get();

      if (shouldBackup) {
        println(docsDownload, "URL: " + url);
        println(docsDownload, page.outerHtml());
        println(docsDownload, "--------------------------");
      }


      return page;
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException ie) {
      ie.printStackTrace();
    }
    return null;
  }


  private void saveDoc(String string) {
    // TODO Auto-generated method stub

  }
}
