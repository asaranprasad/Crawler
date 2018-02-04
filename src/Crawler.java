import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Crawler {
  private CrawlConfig config;

  public Crawler() {
    config = new CrawlConfig();
  }

  public Crawler(CrawlConfig config) {
    this.config = config;
  }

  public void crawlBreadthFirst() {

  }

  public void crawlDepthFirst() {

  }

  public void crawlFocussed() {

  }

  private Document loadFromURL(String URL) {
    try {
      return Jsoup.connect(URL).get();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
}
