import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Crawler {
  private CrawlConfig config;
  private Queue<String> frontier;
  PrintWriter output;
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

  public void crawlBreadthFirst(boolean isFocused) {


    // saveDoc(config.getOutputFolderPath() + "BFSCrawledDocuments.txt");

  }

  public void crawlDepthFirst() throws FileNotFoundException {
    output = new PrintWriter(config.getOutputFolderPath() + "crawlDepthFirst.txt");
    output.println("Count | Text | Depth | URL");

    int depth = 1;
    int pageCount = 1;
    String currentURL = frontier.poll();

    output.println(pageCount + " | Seed | " + depth + " | " + currentURL);

    dfs(loadFromURL(currentURL), depth, pageCount);
    output.close();
  }

  private int dfs(Document page, int depth, int pageCount) {
    List<String[]> urlsInPage = getValidURLsFromPage(page);

    for (int i = 0; i < urlsInPage.size(); i++) {
      output.println((++pageCount) + " | " + urlsInPage.get(i)[1] + " | " + depth + " | "
          + urlsInPage.get(i)[0]);

      if (pageCount >= 1000)
        break;

      if (depth < 6)
        pageCount = dfs(loadFromURL(urlsInPage.get(i)[0]), depth + 1, pageCount);

    }

    return pageCount;
  }

  private List<String[]> getValidURLsFromPage(Document page) {
    // Including only the content portion of the page
    Elements hyperLinks = page.select("#mw-content-text").tagName("a");


    // deduct urls visited

    return null;
  }

  private Document loadFromURL(String URL) {
    try {
      visited.add(URL);
      return Jsoup.connect(URL).get();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }



  private void saveDoc(String string) {
    // TODO Auto-generated method stub

  }

  public Document removeExcludedElements(LocalizationBasePage page,
      String pageString, String pageNameOptional) {
    Document doc = loadFromString(pageString);

    try {
      //default elements for exclusion
      doc.select("[style*='display:none']").not("select").remove();
      doc.select("[style*='display: none']").not("select").remove();
      doc.select("[style*='display :none']").not("select").remove();
      doc.select("[style*='display : none']").not("select").remove();
      doc.select("div.localization-dropdown").remove();
      doc.select("div[class*='disclaimer']").remove();//disclaimer footer
      doc.select("[itemprop*='ddress']").remove();//address type
      doc.select("[itemprop^='street']").remove();//street type
      doc.select(".dropdown-menu").remove(); //sub-menu nav items
      doc.select("[type=hidden]").remove();
      doc.select(".sr-only").remove(); //reading assistance class attribute
      doc.select("address").remove();
      doc.select("sup").remove();//Registered, Trademark and other superscripts
      doc.select("[style*='-10000px']").remove();
      doc.select(".hide").remove();
      doc.select("noscript").remove();
      doc.select("script").remove();
      doc.select("head").remove();
      doc.select("style").remove();
      doc.select("meta").remove();
      doc.select("link").remove();
      doc.select("comment").remove();
      doc.select("CDATA").remove();

      //exclusion list specific to a page
      List<String> exceptionCSSList;
      try {
        if (pageNameOptional == null)
          exceptionCSSList = utils.textFileToList(
              "txt/lcnExcludedCSSList_" + page.getPageName() + ".txt");
        else
          exceptionCSSList = utils.textFileToList("txt/" + pageNameOptional);
      } catch (Exception fe) {
        return doc;
      }
      for (String eachCSSQuery : exceptionCSSList) {
        try {
          // ignoring comments
          if (eachCSSQuery.startsWith("--") || eachCSSQuery.isEmpty()) {
            continue;
          }

          // linkToAnother Exclusion File
          if (eachCSSQuery.startsWith("linkToAnotherFile")) {
            if (!eachCSSQuery.contains("=="))
              continue;
            else {
              doc = removeExcludedElements(page, pageString,
                  eachCSSQuery.split("==")[1]);
              continue;
            }
          }

          // removing DOM elements based on each locator from the exclusion file.
          if (eachCSSQuery.contains("==")) {
            Element element =
                doc.select(eachCSSQuery.split("==")[1]).first();
            if (element != null)
              doc.select(eachCSSQuery.split("==")[1]).remove();
          }

        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return doc;
  }

  public List<String> getIndividualTextsFromPageSource(
      LocalizationBasePage page, String selectiveContainerText) {
    List<String> individualTagText = new ArrayList<String>();
    try {

      String pageString = selectiveContainerText;
      if (pageString == null)
        pageString = page.pageSource();


      Document doc = removeExcludedElements(page, pageString, null);

      Elements all = doc.getAllElements();

      Set<String> elementList = new HashSet<String>();

      for (Element eachElement : all) {
        elementList.add(eachElement.tagName());
      }

      for (String eachTag : elementList) {
        String tagName = eachTag.split(":")[0];
        Elements tag = doc.select(tagName);

        for (Element label : tag) {

          // 1. Inner Tag text
          String labelTxt = label.ownText();
          if (!labelTxt.isEmpty())
            addToStringListExcludingStandardExceptions(labelTxt,
                individualTagText);

          // 2. Alt Tag text

          // 3. Title Tag text
          String labelTitle = label.attr("title").trim();
          if (!labelTitle.isEmpty())
            addToStringListExcludingStandardExceptions(labelTitle,
                individualTagText);
        }
      }
    } catch (Exception e) {
    }
    return individualTagText;
  }



  private boolean isSubStringInList(String biggerString,
      List<String> lstofSubStrings) {
    for (String eachSubString : lstofSubStrings) {
      if (biggerString.contains(eachSubString)
          || biggerString.equals(eachSubString))
        return true;
    }
    return false;
  }

  private void addToStringListExcludingStandardExceptions(String labelText,
      List<String> individualTagText) {
    if (checkForDuplicatedPunctuation(labelText))
      lstLiteralsFoundWithDuplicatedPunctuation.add(labelText);


    labelText = removeLeadingTrailingPunctuation(labelText);

    // configurable exclusion logic
    List<String> exceptionWholeStrings = utils
        .textFileToList("txt/localizationsExcludedWholeStringLiterals.txt");
    List<String> exceptionSubStrings = utils
        .textFileToList("txt/localizationsExcludedSubStringLiterals.txt");

    if (!exceptionWholeStrings.contains(labelText))
      if (!isSubStringInList(labelText, exceptionSubStrings))
        if (labelText.trim().length() > 1) {
          // System.out.println("text: " + labelText);
          addExcludingExceptions(individualTagText, labelText);
        }

  }

  public void addExcludingExceptions(List<String> individualTagText,
      String labelText) {
    try {
      // exception list - Apostrophe s
      if (labelText.equals("s"))
        return;
      // default
      else
        individualTagText.add(labelText);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
