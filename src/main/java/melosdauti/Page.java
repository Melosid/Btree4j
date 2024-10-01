package melosdauti;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Data;

@Data
public class Page implements Comparable<Page>{

  static final int PAGE_SIZE = 512;
  static final int PAGE_HDR = 1 * Integer.BYTES;
  static final int CELL_HDR = 3 * Integer.BYTES;
  static final int MIN_CELL_SIZE = CELL_HDR + Integer.BYTES;
  static final int MX_CELL = (PAGE_SIZE - PAGE_HDR) / MIN_CELL_SIZE;
  static final int USABLE_SPACE = PAGE_SIZE - PAGE_HDR;
  static final int MX_LOCAL_PAYLOAD = (USABLE_SPACE / 4 - CELL_HDR + Integer.BYTES) & ~3;
  static final int FIRST_CELL_BYTE_IDX = 0;
  static final int FREE_CELL_BYTE_IDX = 0;
  static final int RIGHT_CHILD_START = 0;
  static final int RIGHT_CHILD_END = Integer.BYTES;
  static final int LEFT_CHILD_START = 0;
  static final int LEFT_CHILD_END = Integer.BYTES;
  static final int NKEY_START = Integer.BYTES;
  static final int NKEY_END = 2 * Integer.BYTES;
  static final int NDATA_START =  2 * Integer.BYTES;
  static final int NDATA_END = 3 * Integer.BYTES;

  // PageHdr
  private int rightChild;

  private byte[] disk;

  private int pgno;
  private Page parent;
  private int isInit;
  private List<Cell> cells = new ArrayList<>();

  public void init(Page prnt) {
    if (parent != null) return;
    if (isInit == 1) return;

    parent = prnt;
    isInit = 1;
    readDisk();
  }

  public void readDisk() {
    byte[] bRchld = Arrays.copyOfRange(getDisk(), RIGHT_CHILD_START, RIGHT_CHILD_END);
    rightChild = ByteBuffer.wrap(bRchld).getInt();

    byte[] bCells = Arrays.copyOfRange(getDisk(), PAGE_HDR, PAGE_SIZE);

    int offset = 0;

    while (offset + CELL_HDR < bCells.length) {
      byte[] bLchld = Arrays.copyOfRange(bCells, offset + LEFT_CHILD_START, offset + LEFT_CHILD_END);
      byte[] bNKey = Arrays.copyOfRange(bCells, offset + NKEY_START, offset + NKEY_END);
      byte[] bNData = Arrays.copyOfRange(bCells, offset + NDATA_START, offset + NDATA_END);
      int lChld = ByteBuffer.wrap(bLchld).getInt();
      int nKey = ByteBuffer.wrap(bNKey).getInt();
      int nData = ByteBuffer.wrap(bNData).getInt();

      if (nKey <= 0) {
        break;
      }

      int keyIdx = offset + CELL_HDR;
      int dataIdx = offset + CELL_HDR + nKey;
      if (nData != 0) {
        String key = new String(Arrays.copyOfRange(bCells, keyIdx, keyIdx + nKey));
        String value = new String(Arrays.copyOfRange(bCells, dataIdx, dataIdx + nData));
        Cell cell = new Cell(lChld, key, value);
        cells.add(cell);
      }
      offset = offset + CELL_HDR + nKey + nData;
    }
  }

  public void writeDisk() throws IOException {
    ByteArrayOutputStream os = new ByteArrayOutputStream();

    byte[] bRChld = ByteBuffer.allocate(Integer.BYTES).putInt(rightChild).array();
    os.write(bRChld);

    for (Cell cell : cells) {
      os.write(cell.getBytes());
    }

    os.close();
    byte[] result = os.toByteArray();
    disk = result;
  }

  public boolean isOverfull() throws IOException {
    writeDisk();
    return disk.length > USABLE_SPACE;
  }

  public int bFree() throws IOException {
    writeDisk();
    return PAGE_SIZE - disk.length;
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
