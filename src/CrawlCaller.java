import java.util.Scanner;

public class CrawlCaller {


  public static void main(String[] args) {
    CrawlConfig config = new CrawlConfig();

    String option;
    do {
      Crawler crawl = new Crawler(config);
      // print current config to user
      System.out.println("Current Config:");
      config.printConfig();
      System.out.println(
          "Enter a choice:\n 1 - DFS crawl\n 2 - BFS crawl\n 3 - Focused crawl\n q - Quit");
      System.out.println("To change Default config, enter y");
      Scanner scan = new Scanner(System.in);
      option = scan.next();

      switch (option) {
        case "1":
        case "DFS crawl":
          crawl.crawlDepthFirst();
          break;

        case "2":
        case "BFS crawl":
          crawl.crawlBreadthFirst();
          break;

        case "3":
        case "Focused crawl":
          crawl.crawlFocussed();
          break;

        case "y":
          System.out.println("Enter seed URL: ");
          String newSeedURL = scan.next();
          System.out.println("Enter maxDepth: ");
          int newMaxDepth = scan.nextInt();
          System.out
              .println("Enter Focused crawl keywords, comma separated: ");
          String[] newFocusedCrawlKeywords = scan.next().split(",");
          config = new CrawlConfig(newSeedURL, newMaxDepth,
              newFocusedCrawlKeywords);
          continue;

        case "q":
        case "Quit":
          break;
      }
      scan.close();
    } while (option.toLowerCase().startsWith("q"));

  }


}
