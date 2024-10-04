package melosdauti;

import static melosdauti.Page.MX_LOCAL_PAYLOAD;

import lombok.Data;

@Data
public class Cell implements Comparable<Cell> {

  // Cell Header
  private int leftChild;
  private int nKey;
  private int nData;

  private int key;
  private String data;

  public Cell(int key, String data) {
    nKey = 4;
    nData = data.getBytes().length;
    if (nKey + nData > MX_LOCAL_PAYLOAD) {
      throw new RuntimeException("payload is too large");
    }

    this.key = key;
    this.data = data;
  }

  public int size() {
    return 3 * Integer.BYTES + nKey + nData;
  }

  @Override
  public int compareTo(Cell cll) {
    if (key - cll.key < 0) {
      return -1;
    } else if (key - cll.key > 0) {
      return 1;
    }
    return 0;
  }
}
