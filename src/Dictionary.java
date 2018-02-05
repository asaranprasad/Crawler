import java.util.HashSet;

public class Dictionary {
  private HashSet<String> words;

  public Dictionary() {
    String dictionaryPath = "./externalLibrary/words.txt";
    FileUtility ft = new FileUtility();
    words = new HashSet<String>();
    for (String word : ft.textFileToList(dictionaryPath))
      words.add(word.toLowerCase());
  }

  /* Checks for a case insensitive lookup of the
   * given word in the dictionary that is loaded
   */
  public boolean isValidWord(String word) {
    return words.contains(word.toLowerCase());
  }


}
