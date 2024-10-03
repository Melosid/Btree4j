package melosdauti;

import static melosdauti.Page.PAGE_SIZE;
import static melosdauti.Page.USABLE_SPACE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BTree {

  static final int NN = 1;
  static final int NB = NN * 2 + 1;

  public Map<Integer, Page> storage = new HashMap();

  public BTree() {
    Page root = new Page();
    root.setPgno(1);
    root.setParent(null);
    storage.put(1, root);
  }

  public void balance(Page pg) {
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

    if (!pg.isOverfull() && pg.freeBytes() < PAGE_SIZE / 2 && pg.getCells().size() >= 2) {
      return;
    }

    if (parent == null) {
      Page chld;
      if (!pg.isOverfull()) {
        storage.put(pg.getPgno(), pg);
        return;
      }
      chld = allocate();
      chld.setCells(new ArrayList<>(pg.getCells()));
      chld.setRightChild(pg.getRightChild());
      chld.setParent(pg);
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
          Page lChld = storage.get(pgnoLChld);
          if (lChld != null) {
            lChld.setParent(parent);
            pgOld.add(i, lChld);
          }
        }
      } else if (k == parent.getCells().size()) {
        int pgnoRChld = parent.getRightChild();
        if (pgnoRChld == pg.getPgno()) {
          pgOld.add(pg);
        } else {
          Page rChld = storage.get(pgnoRChld);
          if (rChld != null) {
            rChld.setParent(parent);
            pgOld.add(i, rChld);
          }
        }
      } else {
        break;
      }
    }

    for (i = 0; i < pgOld.size(); i++) {
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
      Page n = new Page();
      if (i < pgOld.size()) {
        Page o = pgOld.get(i);
        n.setPgno(o.getPgno());
      } else {
        n = allocate();
      }
      pgNew.add(n);
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

    for (Page page : pgNew) {
      storage.put(page.getPgno(), page);
    }
    balance(parent);
  }

  public Page allocate() {
    Page page = new Page();
    page.setPgno(storage.size() + 1);
    storage.put(page.getPgno(), page);
    return page;
  }

  public void insert(String key, String value) {
    Page root = storage.get(1);

    Page pg = moveTo(key, root);
    Cell cll = new Cell(0, key, value);
    pg.getCells().add(cll);
    Collections.sort(pg.getCells());
    balance(pg);
  }

  public Page moveTo(String key, Page root) {
    for (Cell cell : root.getCells()) {
      if (cell.getKey().equals(key)) {
        return root;
      }
      if (Integer.valueOf(key).compareTo(Integer.valueOf(cell.getKey())) < 0) {
        if (cell.getLeftChild() != 0) {
          Page chld = storage.get(cell.getLeftChild());
          chld.setParent(root);
          return moveTo(key, chld);
        } else {
          return root;
        }
      }
    }
    if (root.getRightChild() != 0) {
      Page chld = storage.get(root.getRightChild());
      chld.setParent(root);
      return moveTo(key, chld);
    } else {
      return root;
    }
  }
}
