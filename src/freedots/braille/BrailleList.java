package freedots.braille;

import java.util.Iterator;

/** Represents a logical unit composed of several smaller objects.
 */
public class BrailleList extends java.util.LinkedList<BrailleSequence>
                         implements BrailleSequence {
  public BrailleList() { super(); }

  private BrailleList parent = null;
  public BrailleList getParent() { return parent; }
  public void setParent(final BrailleList parent) { this.parent = parent; }

  /** Append a sign.
   * This method takes care of inserting {@link GuideDot} objects if
   * required.
   */
  @Override public boolean add(final BrailleSequence item) {
    if (this.needsGuideDot(item)) {
      BrailleSequence dot = new GuideDot();
      dot.setParent(this);
      super.add(dot);
    }

    item.setParent(this);
    return super.add(item);
  }
  @Override public void addLast(final BrailleSequence item) { add(item); }

  public String getDescription() {
    return "Groups several signs as a logical unit.";
  }
  public boolean needsGuideDot(BrailleSequence next) {
    return !isEmpty() && getLast().needsGuideDot(next);
  }
  public Object getScoreObject() { return null; }

  @Override public String toString() {
    StringBuilder sb = new StringBuilder();
    for (BrailleSequence item: this) sb.append(item.toString());
    return sb.toString();
  }
  public int length() { return toString().length(); }
  public char charAt(int index) { return toString().charAt(index); }
  public CharSequence subSequence(int start, int end) {
    return toString().subSequence(start, end);
  }

  /** Retrieves the {@code Atom} at index.
   */
  public Atom getSignAtIndex(final int index) {
    final Iterator<BrailleSequence> iterator = iterator();
    int pos = 0;
    while (iterator.hasNext()) {
      final BrailleSequence current = iterator.next();
      final int length = current.length();
      if (pos + length > index)
        return (current instanceof BrailleList)?
          ((BrailleList)current).getSignAtIndex(index - pos):
          (Atom)current;
      else pos += length;
    }
    return null;
  }
  /** Retrieves the visual score object responsible for the braille at index.
   * @see #getSignAtIndex
   */
  public Object getScoreObjectAtIndex(final int index) {
    for (BrailleSequence sign = getSignAtIndex(index); sign != null;
         sign = sign.getParent()) {
      final Object object = sign.getScoreObject();
      if (object != null) return object;
    }
    return null;
  }

  /**
   * @return -1 if the given score object was not found
   */
  public int getIndexOfScoreObject(final Object scoreObject) {
    if (scoreObject == null)
      throw new NullPointerException("Trying to search for null");

    final Iterator<BrailleSequence> iterator = iterator();
    int index = 0;
    while (iterator.hasNext()) {
      final BrailleSequence current = iterator.next();
      if (current.getScoreObject() == scoreObject) return index;
      if (current instanceof BrailleList) {
        final BrailleList compound = (BrailleList)current;
        final int subIndex = compound.getIndexOfScoreObject(scoreObject);
        if (subIndex >= 0) return index + subIndex;
      }
      index += current.length();
    }

    return -1;
  }
}
