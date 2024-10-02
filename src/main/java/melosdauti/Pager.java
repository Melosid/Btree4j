package melosdauti;

import static melosdauti.Page.PAGE_SIZE;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

public class Pager {

  static final String MAGIC_HEADER = "** DB **";

  private final RandomAccessFile raf;

  public Pager(String fp) throws IOException {
    File file = new File(fp);
    raf = new RandomAccessFile(file, "rw");
  }

  public int nextFreePgno() throws IOException {
    return (int) raf.length() / PAGE_SIZE;
  }

  public Page allocate() throws IOException {
    int pgno = nextFreePgno();
    raf.seek((long) pgno * PAGE_SIZE);
    raf.write(new byte[PAGE_SIZE]);
    byte[] data = new byte[PAGE_SIZE];
    raf.read(data);
    Page page = new Page();
    page.setDisk(data);
    page.setPgno(pgno);
    return page;
  }

  public boolean getZero() throws IOException {
    raf.seek(0);
    byte[] data = new byte[PAGE_SIZE];
    raf.read(data);
    String hdr = new String(Arrays.copyOfRange(data, 0, MAGIC_HEADER.getBytes().length));
    if (!hdr.equals(MAGIC_HEADER)) {
        return false;
    }
    return true;
  }

  public boolean setZero() throws IOException {
    raf.seek(0);
    raf.write(MAGIC_HEADER.getBytes());
    raf.seek(PAGE_SIZE);
    return true;
  }

  public Page getRoot() throws IOException {
    if (raf.length() <= PAGE_SIZE) {
      return null;
    }
    raf.seek(PAGE_SIZE);
    byte[] data = new byte[PAGE_SIZE];
    raf.read(data);
    Page page = new Page();
    page.setDisk(data);
    page.setPgno(1);
    return page;
  }

  public Page setRoot() throws IOException {
    raf.seek(PAGE_SIZE);
    byte[] data = new byte[PAGE_SIZE];
    raf.read(data);
    Page page = new Page();
    page.setDisk(data);
    page.setPgno(1);
    raf.write(page.getDisk());
    return page;
  }

  public Page get(int pgno) throws IOException {
    if (pgno == 0) {
      return null;
    }
    raf.seek((long) pgno * PAGE_SIZE);
    byte[] data = new byte[PAGE_SIZE];
    raf.read(data);
    Page page = new Page();
    page.setDisk(data);
    page.setPgno(pgno);
    return page;
  }

  public void save(Page page) throws IOException {
    int pgno = page.getPgno();
    page.writeDisk();
    raf.seek((long) pgno * PAGE_SIZE);
    byte[] result = new byte[PAGE_SIZE];
    int bLength = page.getDisk().length;
    int limit = bLength < PAGE_SIZE ? bLength : PAGE_SIZE;
    System.arraycopy(page.getDisk(), 0, result, 0, limit);
    raf.write(result);
  }

  public void free(int pgno) throws IOException {
    raf.seek((long) pgno * PAGE_SIZE);
    raf.write(new byte[PAGE_SIZE]);
  }

  public void reset() throws IOException {
    raf.setLength(0);
  }
}
