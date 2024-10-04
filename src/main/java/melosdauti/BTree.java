package melosdauti;

import static melosdauti.Page.PAGE_SIZE;
import static melosdauti.Page.USABLE_SPACE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BTree {

  // NN is number of neighbours in each side of the page.
  // NB number of pages that participate in the balancing operation.
  static final int NN = 1;
  static final int NB = NN * 2 + 1;

  // Storage meant to resemble a file.
  public Map<Integer, Page> storage = new HashMap();

  public BTree() {
    Page root = new Page();
    root.setPgno(1);
    root.setParent(null);
    storage.put(1, root);
  }

  /**
   * Balance page after insert or delete operations
   *
   * @param pg
   */
  private void balance(Page pg) {
    /**
     * i, j, k - iterators
     * idx - index of current page on the parent
     * nxDiv - next divider cell
     * div - divider cells (up to NB cells)
     * pgOld - pages before balance
     * pgNew - pages after balance
     * cells - all cells in pgOld + divider cells
     * szNew - size of each pgNew
     * cntNew - first cell of next page on pgNew
     */
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
        return;
      }
      /**
       * If root page is overfull copy the cells to right child and continue.
       */
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

    /**
     * Find index of current page on parent.
     */
    for (idx = 0; idx < parent.getCells().size(); idx++) {
      if (parent.getCells().get(idx).getLeftChild() == pg.getPgno()) {
        break;
      }
    }

    /**
     * Find divider cells and their respective child pages.
     */
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

    /**
     * Find all cells on div and pgOld.
     */
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

    /**
     * Find the best way to share cells between the new pages.
     * Make sure the pages don't surpass usable space and are occupy preferably over half
     * of usable space
     */
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

    /**
     * Allocate new pages. Re-use old ones when possible.
     */
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

    /**
     * Add cells to new pages and divider cells to parent page accordingly.
     * Make sure the new divider cells point back to the new pages.
     */
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

    /**
     * Make sure the last page in the balance points to the original right child.
     * If last cell on parent was a divider cell, link the right child of page to the last new Page,
     * otherwise link the last divider cell to the last new Page
     */
    pgNew.getLast().setRightChild(pgOld.getLast().getRightChild());
    if (nxDiv == parent.getCells().size()) {
      parent.setRightChild(pgNew.getLast().getPgno());
    } else {
      parent.getCells().get(nxDiv).setLeftChild(pgNew.getLast().getPgno());
    }

    /**
     * Save changes
     */
    for (Page page : pgNew) {
      storage.put(page.getPgno(), page);
    }
    balance(parent);
  }

  /**
   * Create new Page.
   *
   * @return
   */
  private Page allocate() {
    Page page = new Page();
    page.setPgno(storage.size() + 1);
    storage.put(page.getPgno(), page);
    return page;
  }

  /**
   * Insert new entry
   *
   * @param key
   * @param value
   */
  public void insert(int key, String value) {
    Page root = storage.get(1);

    if (get(key) != null) {
      delete(key);
    }

    Page pg = moveTo(key, root);
    pg.getCells().add(new Cell(key, value));
    Collections.sort(pg.getCells());
    balance(pg);
  }

  /**
   * Get entry by key
   *
   * @param key
   * @return
   */
  public Cell get(int key) {
    Page root = storage.get(1);

    Page pg = moveTo(key, root);
    for (Cell c : pg.getCells()) {
      if (key == c.getKey()) {
        return c;
      }
    }
    return null;
  }

  /**
   * Delete entry
   *
   * @param key
   */
  public void delete(int key) {
    Page root = storage.get(1);

    Cell cll = get(key);
    if (get(key) != null) {
      Page pg = moveTo(key, root);
      if (cll.getLeftChild() == 0) {
        pg.getCells().remove(cll);
        balance(pg);
      } else {
        Page next = moveToNext(cll.getKey(), pg);
        Cell nxCll = null;
        for (Cell c : next.getCells()) {
          if (c.getLeftChild() == 0) {
            nxCll = c;
            break;
          }
        }
        pg.getCells().remove(cll);
        nxCll.setLeftChild(cll.getLeftChild());
      }
    }
  }

  /**
   * Move to next entry
   *
   * @param key
   * @param root
   * @return Page where entry with next key resides
   */
  private Page moveToNext(int key, Page root) {
    Cell nx = null;
    for (Cell c : root.getCells()) {
      if (c.getKey() > key) {
        nx = c;
      }
      if (c.getKey() > key && c.getLeftChild() == 0) {
        return root;
      }
      if (c.getKey() > key && c.getLeftChild() != 0) {
        Page lChld = storage.get(c.getLeftChild());
        lChld.setParent(root);
        return moveToNext(key, lChld);
      }
    }
    if (nx == null) {
      if (root.getRightChild() != 0) {
        Page rChld = storage.get(root.getRightChild());
        rChld.setParent(root);
        return moveToNext(key, rChld);
      } else {
        if (root.getParent() != null) {
          return moveToNext(key, root.getParent());
        } else {
          return root;
        }
      }
    }
    return null;
  }

  /**
   * Move to key
   *
   * @param key
   * @param root
   * @return Page where entry with that key resides
   */
  private Page moveTo(int key, Page root) {
    for (Cell cell : root.getCells()) {
      if (key == cell.getKey()) {
        return root;
      }
      if (key - cell.getKey() < 0) {
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
