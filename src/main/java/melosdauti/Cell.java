package melosdauti;

import static melosdauti.Page.MX_LOCAL_PAYLOAD;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Cell implements Comparable<Cell> {

  // CellHdr
  private int leftChild;
  private int nKey;
  private int nData;

  private String key;
  private String data;

  public Cell(int leftChild, String key, String data) {
    nKey = key.getBytes().length;
    nData = data.getBytes().length;
    if (nKey + nData > MX_LOCAL_PAYLOAD) {
      throw new RuntimeException("payload is too large");
    }

    this.leftChild = leftChild;
    this.key = key;
    this.data = data;
  }

  public int size() {
    return 3 * Integer.BYTES + nKey + nData;
  }

  public byte[] getBytes() throws IOException {
    ByteArrayOutputStream os = new ByteArrayOutputStream();

    byte[] bLChld = ByteBuffer.allocate(Integer.BYTES).putInt(leftChild).array();
    byte[] bNKey = ByteBuffer.allocate(Integer.BYTES).putInt(nKey).array();
    byte[] bNData = ByteBuffer.allocate(Integer.BYTES).putInt(nData).array();
    os.write(bLChld);
    os.write(bNKey);
    os.write(bNData);
    os.write(key.getBytes());
    os.write(data.getBytes());

    return os.toByteArray();
  }

  @Override
  public int compareTo(Cell cll) {
    if (key.compareTo(cll.key) < 0) {
      return -1;
    } else if (key.compareTo(cll.key) > 0) {
      return 1;
    }
    return 0;
  }
}
