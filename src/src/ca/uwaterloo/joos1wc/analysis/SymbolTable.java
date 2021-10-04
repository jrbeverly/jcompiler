package ca.uwaterloo.joos1wc.analysis;

import java.util.Iterator;
import java.util.NoSuchElementException;

import ca.uwaterloo.joos1wc.ast.INamedEntityNode;

/**
 * The Symbol Table, implemented as a sort of linked-list of linked-lists. This choice was made to allow the tables to
 * be added first, and then the symbols themselves to be added later with little effort. Since the symbols don't link
 * between tables, inserting a new symbol in the middle doesn't require any other table to change.
 *
 */
public class SymbolTable implements Iterable<SymbolTable.Entry> {
  // nextTable keeps a reference to the most recent scope before this one
  private final SymbolTable next;
  private final Entry entry;

  /**
   * The empty constructor is used for the global scope
   */
  public SymbolTable() {
    this.next = null;
    this.entry = null;
  }

  /**
   * The single parameter constructor is used for everything else
   * 
   * @param next
   *          The most recent scope before this one
   */
  public SymbolTable(SymbolTable next, String key, INamedEntityNode decl) {
    this.next = next;
    this.entry = new Entry(key, decl);
  }

  /**
   * Returns null if no symbol with a matching key is found.
   * 
   * @param key
   *          The name of the symbol to find
   * @return The symbol object representing the first encountered symbol with a matching key
   */
  public INamedEntityNode get(String key) {
    assert key != null;
    for (Entry entry : this) {
      if (entry != null && key.equals(entry.key)) {
        return entry.decl;
      }
    }
    return null;
  }

  public SymbolTable.Entry head() {
    return entry;
  }

  public Iterator<Entry> iterator() {
    return new SymbolTableIterator(this);
  }

  private static class SymbolTableIterator implements Iterator<Entry> {

    private SymbolTable currentNode;

    public SymbolTableIterator(SymbolTable node) {
      currentNode = node;
    }

    @Override
    public boolean hasNext() {
      return currentNode != null && currentNode.entry != null;
    }

    @Override
    public Entry next() {
      if (!hasNext())
        throw new NoSuchElementException();
      Entry ret = currentNode.entry;
      assert ret != null;
      currentNode = currentNode.next;
      return ret;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }

  }

  public static class Entry {
    public final String key;
    public final INamedEntityNode decl;

    public Entry(String key, INamedEntityNode decl) {
      this.key = key;
      this.decl = decl;
    }
  }

}
