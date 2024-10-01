package melosdauti;

import java.io.IOException;

public class Main {

  public static void main(String[] args) throws IOException {
    String fp = "/home/melos/study/database";
    BTree bTree = new BTree(fp);

    for (int i=1; i <= 1000; i++) {
      String key = String.valueOf(i);
      String data = "The string that I want to save with key " + key;
      bTree.insert(key, data);
    }

    // testing
    Pager pager = new Pager(fp);
    Page one = pager.get(1);
    one.readDisk();
    System.out.println("one " + one);
    System.out.println("one cells size: " + one.getCells().size());
  }
}
