package melosdauti;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class Page implements Comparable<Page> {

  static final int PAGE_SIZE = 256;
  static final int PAGE_HDR = Integer.BYTES;
  static final int CELL_HDR = 3 * Integer.BYTES;
  static final int MIN_CELL_SIZE = CELL_HDR + Integer.BYTES;
  static final int MX_CELL = (PAGE_SIZE - PAGE_HDR) / MIN_CELL_SIZE;
  static final int USABLE_SPACE = PAGE_SIZE - PAGE_HDR;
  static final int MX_LOCAL_PAYLOAD = (USABLE_SPACE / 4 - CELL_HDR + Integer.BYTES) & ~3;

  // PageHdr
  private int rightChild;

  private byte[] disk;

  private int pgno;
  private Page parent;
  private int isInit;
  private List<Cell> cells = new ArrayList<>();

  public void init(Page prnt) {
    parent = prnt;
    isInit = 1;
  }

  public int size() {
    int sz = PAGE_HDR;
    for (Cell cell : cells) {
      sz += cell.size();
    }
    return sz;
  }

  public boolean isOverfull() {
    return size() > USABLE_SPACE;
  }

  public int freeBytes() {
    return PAGE_SIZE - size();
  }

  @Override
  public int compareTo(Page page) {
    if (pgno < page.pgno) {
      return -1;
    } else if (pgno > page.pgno) {
      return 1;
    }
    return 0;
  }
}
