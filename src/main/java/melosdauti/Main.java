package melosdauti;

import java.io.IOException;

public class Main {

  public static void main(String[] args) throws IOException {
    String fp = "/home/melos/study/database";
    BTree bTree = new BTree(fp);

    int[] nums = {1, 11, 21, 2, 12, 22, 3, 13, 23, 4, 14, 24, 5, 15, 25, 6, 16, 26, 7, 17, 27, 8, 18, 28, 9, 19, 29, 10, 20, 30};

    for (int i=1; i <= nums.length; i++) {
      String key = String.valueOf(nums[i-1]);
      String data = "The string that I want to save with key " + key;
      bTree.insert(key, data);
    }

    // testing

    int totalCells = 0;
    Pager pager = new Pager(fp);
    for (int i = 1; i <= 15; i++) {
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
