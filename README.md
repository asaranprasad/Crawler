# Web Crawler - Focused Crawling - Java Implementation

Task1: Crawling the documents using both Breadth-First and Depth-First traversal techniques.
Task2: Crawling the documents with respect to a set of keywords and considering valid variations of the keywords.

## Setting up

The ./src/ folder consists of source code files
```
1. CrawlCaller.java - Consists of the Main Method to kick start the program.
2. CrawlConfig.java - Contains configurable parameters for the call.
3. Crawler.java     - Has the crawler logic implemented.
4. FileUtility.java - Utility file for accessing flat files.
5. Dictionary.java  - Accesses and maintains a dictionary of English words.
6. Tests.java       - Consists of tests to validate the crawler ouputs.
```

The ./output/ folder consists of sample outputs of the call
```
1. crawlBreadthFirst.txt - First 1000 urls from breadth first traversal.
2. crawlDepthFirst.txt   - First 1000 urls from depth first traversal.
3. crawlFocused.txt      - First 1000 urls from focused crawling.
```

### External Libraries Referenced

The following external library might need to be referenced to the build path, or via using a Maven dependency.

1. JSoup

```
<dependency>
    <groupId>org.jsoup</groupId>
    <artifactId>jsoup</artifactId>
    <version>1.9.2</version>
</dependency>
```


2. English Words Collection - words.txt

```
Citation: https://github.com/dwyl/english-words/blob/master/words.txt
```

The above libraries have been included in the ./externalLibrary/ folder

### Maximum Depth Reached

Task 1:
```
DFS         : 6
BFS         : 2
```

Task 2:
```
FocusedCrawl: 4
```
