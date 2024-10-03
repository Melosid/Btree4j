package melosdauti;

import static melosdauti.Page.PAGE_SIZE;
import static melosdauti.Page.USABLE_SPACE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class BTree {
  static final int NN = 1;
  static final int NB = NN * 2 + 1;

  private final Pager pager;

  public BTree(String fp) throws IOException {
    pager = new Pager(fp);
    pager.reset();
    if (!pager.getZero()) {
      pager.setZero();
    }
  }

  public void balance(Page pg) throws IOException {
    Page parent = pg.getParent();
    int i, j, k;
    int idx;
    int nxDiv;
    List<Cell> div = new ArrayList<>();
    List<Page> pgOld = new ArrayList<>();
    List<Cell> cells = new ArrayList<>();
    int[] szNew = new int[NB + 1];
    int[] cntNew = new int[NB + 1];
    List<Page> pgNew = new ArrayList<>();

    if (!pg.isOverfull() && pg.bFree() < PAGE_SIZE / 2 && pg.getCells().size() >= 2) {
      System.out.println("no need to balance");
      pager.save(pg);
      return;
    }
    System.out.println("need to balance");

    if (parent == null) {
      Page chld;
      if (!pg.isOverfull()) {
        pager.save(pg);
        return;
      }
      chld = pager.allocate();
      chld.setCells(new ArrayList<>(pg.getCells()));
      chld.setRightChild(pg.getRightChild());
      chld.setParent(pg);
      chld.setIsInit(1);
      pg.getCells().clear();
      pg.setRightChild(chld.getPgno());
      pg.setParent(parent);
      parent = pg;
      pg = chld;
    }

    for (idx = 0; idx < parent.getCells().size(); idx++) {
      if (parent.getCells().get(idx).getLeftChild() == pg.getPgno()) {
        break;
      }
    }

    nxDiv = idx - NN;
    if (nxDiv + NB > parent.getCells().size()) {
      nxDiv = parent.getCells().size() - NB + 1;
    }
    if (nxDiv < 0) {
      nxDiv = 0;
    }
    for (i = 0, k = nxDiv; i < NB; i++, k++) {
      if (k < parent.getCells().size()) {
        div.add(i, parent.getCells().get(k));
        int pgnoLChld = div.get(i).getLeftChild();
        if (pgnoLChld == pg.getPgno()) {
          pgOld.add(pg);
        } else {
          Page lChld = pager.get(pgnoLChld);
          if (lChld != null) {
            lChld.init(parent);
            pgOld.add(i, lChld);
          }
        }
      } else if (k == parent.getCells().size()) {
        int pgnoRChld = parent.getRightChild();
        if (pgnoRChld == pg.getPgno()) {
          pgOld.add(pg);
        } else {
          Page rChld = pager.get(pgnoRChld);
          if (rChld != null) {
            rChld.init(parent);
            pgOld.add(i, rChld);
          }
        }
      } else {
        break;
      }
    }

    for (i = 0; i < pgOld.size(); i++ ) {
      Page old = pgOld.get(i);
      for (j = 0; j < old.getCells().size(); j++) {
        cells.add(old.getCells().get(j));
      }
      if (i < pgOld.size() - 1) {
        cells.add(div.get(i));
        parent.getCells().remove(nxDiv);
        cells.getLast().setLeftChild(old.getRightChild());
      }
    }
    Collections.sort(cells);

    int subtotal;

    for (subtotal = k = i = 0; i < cells.size(); i++) {
      subtotal += cells.get(i).size();
      if (subtotal > USABLE_SPACE) {
        szNew[k] = subtotal - cells.get(i).size();
        cntNew[k] = i;
        subtotal = 0;
        k++;
      }
    }
    szNew[k] = subtotal;
    cntNew[k] = cells.size();
    k++;
    for (i = k - 1; i > 0; i--) {
      while (szNew[i] < USABLE_SPACE / 2) {
        cntNew[i - 1]--;
        szNew[i] += cells.get(cntNew[i - 1]).size();
        szNew[i - 1] -= cells.get(cntNew[i - 1] - 1).size();
      }
    }

    for (i = 0; i < k; i++) {
      if (i < pgOld.size()) {
        pgNew.add(i, pgOld.get(i));
        pgNew.get(i).setCells(new ArrayList<>());
      } else {
        pgNew.add(i, pager.allocate());
      }
      pgNew.get(i).setIsInit(1);
    }


    j = 0;
    for (i = 0; i < pgNew.size(); i++) {
      Page pgn = pgNew.get(i);
      while (j < cntNew[i]) {
        pgn.getCells().add(cells.get(j));
        j++;
      }
      if (i < pgNew.size() - 1 && j < cells.size()) {
        pgn.setRightChild(cells.get(j).getLeftChild());
        cells.get(j).setLeftChild(pgNew.get(i).getPgno());
        parent.getCells().add(nxDiv, cells.get(j));
        j++;
        nxDiv++;
      }
    }

    pgNew.getLast().setRightChild(pgOld.getLast().getRightChild());
    if (nxDiv == parent.getCells().size()) {
      parent.setRightChild(pgNew.getLast().getPgno());
    } else {
      parent.getCells().get(nxDiv).setLeftChild(pgNew.getLast().getPgno());
    }

    for (Page page: pgNew) {
      pager.save(page);
    }
    balance(parent);
  }

  public void insert(String key, String value) throws IOException {
    if (pager.getRoot() == null) {
      pager.setRoot();
    }

    Page root = pager.getRoot();
    root.init(null);

    Page pg = moveTo(key, root);
    Cell cll = new Cell(0, key, value);
    pg.getCells().add(cll);
    Collections.sort(pg.getCells());
    balance(pg);
  }

  public Page moveTo(String key, Page root) throws IOException {
    for (Cell cell: root.getCells()) {
      if (cell.getKey().equals(key)) {
        return root;
      }
      if (key.compareTo(cell.getKey()) < 0) {
        if (cell.getLeftChild() != 0) {
          Page chld = pager.get(cell.getLeftChild());
          chld.init(root);
          moveTo(key, chld);
        } else {
          return root;
        }
      }
    }
    if (root.getRightChild() != 0) {
      Page chld = pager.get(root.getRightChild());
      chld.init(root);
      moveTo(key, chld);
    }
    return root;
  }
}
