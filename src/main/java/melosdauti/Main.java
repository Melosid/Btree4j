package melosdauti;

import java.io.IOException;

public class Main {

  public static void main(String[] args) throws IOException {
    String fp = "/home/melos/study/database";
    BTree bTree = new BTree(fp);

    for (int i=1; i <= 200; i++) {
      String key = String.valueOf(i);
      String data = "The string that I want to save with key " + key;
      bTree.insert(key, data);
    }

    // testing

    int totalCells = 0;
    Pager pager = new Pager(fp);
    for (int i = 1; i <= 100; i++) {
      System.out.println("i: " + i);
      Page pg = pager.get(i);
      pg.readDisk();
      System.out.println("pgno: " + pg.getPgno());
      System.out.println("cells: " + pg.getCells());
      totalCells += pg.getCells().size();
      System.out.println("rightchild: " + pg.getRightChild());
    }
    System.out.println("totalCells: " + totalCells);
  }
}
