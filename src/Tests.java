import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

public class Tests {

  public static void main(String[] args) {
    CrawlConfig config = new CrawlConfig();
    boolean testStatus = true;
    testStatus = findDuplicates(textFileToList(config.getBreadthFirstOutputPath()));
    testStatus =
        testStatus && findDuplicates(textFileToList(config.getDepthFirstOutputPath()));
    testStatus =
        testStatus && findDuplicates(textFileToList(config.getFocusedCrawlOutputPath()));

    String[] variations = {"lunar", "moon"};
    testStatus =
        testStatus && urlsHaveValidVariations(
            textFileToList(config.getFocusedCrawlOutputPath()), variations);

    String status = testStatus ? "Passed" : "Failed";
    System.out.println("Test Status: " + status);

  }

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

  private static boolean urlsHaveValidVariations(List<String> textFileToList,
      String[] variations) {
    boolean retVal = true;
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
          System.out.println("No valid variations found: " + inputLine);
          retVal = false;
        }
      }
    }
    return retVal;
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
