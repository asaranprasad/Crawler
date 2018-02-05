import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

public class Tests {

  public static void main(String[] args) {
    CrawlConfig config = new CrawlConfig();
    boolean testStatus = true;

    // 1. Find if duplicates present
    testStatus = findDuplicates(textFileToList(config.getBreadthFirstOutputPath()));
    testStatus =
        testStatus && findDuplicates(textFileToList(config.getDepthFirstOutputPath()));
    testStatus =
        testStatus && findDuplicates(textFileToList(config.getFocusedCrawlOutputPath()));



    // 2. Find URL overlap between BreadthFirst and DepthFirst traversals
    findURLOverlap(textFileToList(config.getBreadthFirstOutputPath()),
        textFileToList(config.getDepthFirstOutputPath()));

    // 3. Verify if valid variations are present
    System.out.println("Focused Crawl - Valid Variations");
    String[] variations = {"lunar", "moon"};
    testStatus =
        testStatus && urlsHaveValidVariations(
            textFileToList(config.getFocusedCrawlOutputPath()), variations, true);


    // 4. Percentage of valid variations - BFS
    System.out.println("BFS - Valid Variations");
    variations = new String[] {"solar", "eclipse"};
    urlsHaveValidVariations(textFileToList(config.getBreadthFirstOutputPath()),
        variations, false);

    // 5. Percentage of valid variations - DFS
    System.out.println("DFS - Valid Variations");
    variations = new String[] {"solar", "eclipse"};
    urlsHaveValidVariations(textFileToList(config.getDepthFirstOutputPath()),
        variations, false);

    String status = testStatus ? "Passed" : "Failed";
    System.out.println("Test Status: " + status);


  }

  /* Find if the output of the crawls generate duplicate URLs */
  private static boolean findDuplicates(List<String> textFileToList) {
    HashSet<String> visited = new HashSet<String>();
    boolean retVal = true;
    for (String inputLine : textFileToList) {
      if (inputLine.trim().length() == 0)
        continue;
      String url =
          inputLine.substring(inputLine.lastIndexOf("|") + 1, inputLine.length()).trim();
      if (visited.contains(url)) {
        System.out.println("Duplicate found: " + url);
        retVal = false;
      } else
        visited.add(url);
    }
    return retVal;
  }

  /* Find if the focused crawl does the job with valid variations of the keywords */
  private static boolean urlsHaveValidVariations(List<String> textFileToList,
      String[] variations, boolean printToConsole) {
    boolean retVal = true;
    int countInvalidURLs = 0;
    for (String inputLine : textFileToList) {
      if (inputLine.trim().length() == 0)
        continue;
      if (inputLine.contains("Count | Text") || inputLine.contains("1 | Seed"))
        continue;

      int attempt = 0;
      for (String keyword : variations) {
        keyword = keyword.toLowerCase().trim();
        if (!inputLine.toLowerCase().contains(keyword))
          attempt++;
        if (attempt == variations.length) {
          if (printToConsole)
            System.out.println("No valid variations found: " + inputLine);
          retVal = false;
          countInvalidURLs++;
        }
      }
    }

    System.out.println("Percentage of valid URLs from the crawl: "
        + (((float) (textFileToList.size() - countInvalidURLs)
            / (float) textFileToList.size()) * 100));

    return retVal;
  }


  /* Find URL overlap between BreadthFirst and DepthFirst traversals */
  private static void findURLOverlap(List<String> list1,
      List<String> list2) {
    HashSet<String> urlSet1 = new HashSet<String>();
    HashSet<String> urlSet2 = new HashSet<String>();
    for (String inputLine : list1)
      urlSet1.add(
          inputLine.substring(inputLine.lastIndexOf("|") + 1, inputLine.length()).trim());
    for (String inputLine : list2)
      urlSet2.add(
          inputLine.substring(inputLine.lastIndexOf("|") + 1, inputLine.length()).trim());

    List<String> overLappingURLs = new ArrayList<String>();
    for (String url : urlSet1) {
      if (urlSet2.contains(url)) {
        System.out.println("overlapping url found: " + url);
        overLappingURLs.add(url);
      }
    }

    System.out.println(
        "Percentage of URL overlap: "
            + (((float) overLappingURLs.size() / (float) list1.size()) * 100));
  }


  public static List<String> textFileToList(String filePath) {
    List<String> lines = new ArrayList<String>();
    try {
      Scanner sc = new Scanner(new File(filePath));
      while (sc.hasNextLine())
        lines.add(sc.nextLine());
      sc.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return lines;
  }
}
