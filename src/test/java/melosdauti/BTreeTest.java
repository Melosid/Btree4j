package melosdauti;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public class BTreeTest {
  private BTree bTree;
  private final int[] KEYS = {1, 11, 21, 2, 12, 22, 3, 13, 23, 4, 14, 24, 5, 15, 25, 6, 16, 26, 7, 17, 27, 8, 18, 28, 9, 19, 29, 10,
      20, 30};

  @Before
  public void setUp() {
    bTree = new BTree();
    for (int i = 1; i <= KEYS.length; i++) {
      String data = "The string that I want to save with key " + i;
      bTree.insert(i, data);
    }
  }

  @Test
  public void treeIsBalancedAfterInsertion() {
    Page root = bTree.storage.get(1);
    assertThat(root).isNotNull();

    Cell rootCell = root.getCells().get(0);

    Page lChild = bTree.storage.get(rootCell.getLeftChild());
    Cell lChildCell = lChild.getCells().get(0);

    assertThat(lChildCell).isLessThan(rootCell);

    Page rChild = bTree.storage.get(root.getRightChild());
    Cell rChildCell = rChild.getCells().get(0);
    assertThat(rChildCell).isGreaterThan(rootCell);

    root.getCells();
  }

  @Test
  public void treeIsBalancedAfterRandomOperations() {
    bTree.delete(27);
    bTree.delete(8);
    bTree.delete(11);
    bTree.insert(11, "Newly re-inserted key 11");
    bTree.insert(8, "Newly re-inserted key 8");
    assertThat(bTree.get(14).getData()).isEqualTo("The string that I want to save with key 14");
    assertThat(bTree.get(27)).isNull();
    assertThat(bTree.get(8).getData()).isEqualTo("Newly re-inserted key 8");

    int totalCells = 0;
    for (var entry : bTree.storage.entrySet()) {
      totalCells += entry.getValue().getCells().size();
    }
    assertThat(totalCells).isEqualTo(29);
  }

}
