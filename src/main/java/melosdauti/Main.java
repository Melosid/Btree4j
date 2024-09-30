package melosdauti;

import java.io.IOException;

public class Main {

  public static void main(String[] args) throws IOException {
    String fp = "/home/melos/study/database";
    BTree bTree = new BTree(fp);

    for (int i=1; i <= 10; i++) {
      String key = String.valueOf(i);
      String data = "The string that I want to save with key " + key;
      bTree.insert(key, data);
    }
  }
}
