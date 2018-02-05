import java.util.Scanner;

public class CrawlCaller {


  public static void main(String[] args) {
    try {
      CrawlConfig config = new CrawlConfig();
      Scanner scan = new Scanner(System.in);
      String option;
      do {
        Crawler crawl = new Crawler(config);

        // print current config to user
        System.out.println("Current Config:");
        config.printConfig();
        System.out.println(
            "Enter a choice:\n 1 - DFS crawl\n 2 - BFS crawl with document backup");
        System.out.println(
            " 3 - BFS crawl without document backup\n 4 - Focused crawl\n q - Quit");
        System.out.println("To change Default config, enter y");

        option = scan.next();

        // timer start
        long startTime = System.nanoTime();

        switch (option) {
          case "1":
          case "DFS crawl":
            crawl.crawlDepthFirst();
            break;

          case "2":
          case "BFS crawl with document backup":
            crawl.crawlBreadthFirst(false, true);
            break;

          case "3":
          case "BFS crawl without document backup":
            crawl.crawlBreadthFirst(false, false);
            break;

          case "4":
          case "Focused crawl":
            crawl.crawlBreadthFirst(true, false);
            break;

          case "y":
            System.out.println("Enter seed URL: ");
            String newSeedURL = scan.next();
            System.out.println("Enter maxDepth: ");
            int newMaxDepth = scan.nextInt();
            System.out.println("Enter pageCount: ");
            int newPageCount = scan.nextInt();
            System.out
                .println("Enter Focused crawl keywords, comma separated: ");
            String[] newFocusedCrawlKeywords = scan.next().split(",");
            config = new CrawlConfig(newSeedURL, newMaxDepth, newPageCount,
                newFocusedCrawlKeywords);
            continue;

          case "q":
          case "Quit":
            break;
        }
        long endTime = System.nanoTime();

        System.out.println(
            "Time taken for the operation in nanosecs: " + (endTime - startTime));

      } while (!option.toLowerCase().startsWith("q"));
      scan.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

  }


}
