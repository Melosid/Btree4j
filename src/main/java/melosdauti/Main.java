package melosdauti;

public class Main {

  public static void main(String[] args) {
    BTree bTree = new BTree();

    int[] nums = {1, 11, 21, 2, 12, 22, 3, 13, 23, 4, 14, 24, 5, 15, 25, 6, 16, 26, 7, 17, 27, 8, 18, 28, 9, 19, 29, 10,
        20, 30};

    for (int i = 1; i <= nums.length; i++) {
      int key = nums[i - 1];
      String data = "The string that I want to save with key " + key;
      bTree.insert(key, data);
    }

    // test insert
    int totalCells = 0;
    for (int i = 1; i <= bTree.storage.size(); i++) {
      System.out.println("i: " + i);
      Page pg = bTree.storage.get(i);
      System.out.println("pgno: " + pg.getPgno());
      System.out.println("cells: " + pg.getCells());
      totalCells += pg.getCells().size();
      System.out.println("rightchild: " + pg.getRightChild());
    }
    System.out.println("totalCells: " + totalCells);

    // test get
    System.out.println("get key 17: " + bTree.get(17));
    System.out.println("delete key 17: ");
    bTree.delete(17);
    System.out.println("update key 4: ");
    bTree.insert(4, "My updated string with key 4");

    totalCells = 0;
    for (int i = 1; i <= bTree.storage.size(); i++) {
      System.out.println("i: " + i);
      Page pg = bTree.storage.get(i);
      System.out.println("pgno: " + pg.getPgno());
      System.out.println("cells: " + pg.getCells());
      totalCells += pg.getCells().size();
      System.out.println("rightchild: " + pg.getRightChild());
    }
    System.out.println("totalCells: " + totalCells);
  }
}
