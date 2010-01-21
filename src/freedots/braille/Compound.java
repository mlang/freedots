package freedots.braille;

import java.util.Iterator;
import java.util.LinkedList;

/** Represents a logical unit comprised of several smaller objects.
 */
public class Compound extends LinkedList<Sign> implements Sign {
  Compound() { super(); }

  private Sign parent = null;
  public Sign getParent() { return parent; }
  public void setParent(final Sign parent) { this.parent = parent; }

  /** Append a sign.
   * This method takes care of inserting {@link GuideDot} objects if
   * required.
   */
  @Override public boolean add(Sign item) {
    if (!isEmpty()) {
      final Sign last = getLast();
      if (last.mightNeedAdditionalDot()) {
        String string = item.toString();
        if (string.length() > 0) {
          char ch = string.charAt(0);
          if (((int)ch & 0X2807) > 0X2800)
            super.add(new GuideDot());
        }
      }
    }
    item.setParent(this);
    return super.add(item);
  }
  @Override public void addLast(Sign item) { add(item); }

  public String getDescription() {
    return "Groups several signs as a logical unit.";
  }
  public boolean mightNeedAdditionalDot() {
    if (!isEmpty()) return getLast().mightNeedAdditionalDot();
    return false;
  }
  public Object getScoreObject() { return null; }

  @Override public String toString() {
    StringBuilder sb = new StringBuilder();
    for (Sign item: this) sb.append(item.toString());
    return sb.toString();
  }
  public int length() { return toString().length(); }
  public char charAt(int index) { return toString().charAt(index); }
  public CharSequence subSequence(int start, int end) {
    return toString().subSequence(start, end);
  }

  /** Retrieves the {@code Atom} at index.
   */
  public Sign getSignAtIndex(final int index) {
    final Iterator<Sign> iterator = iterator();
    int pos = 0;
    while (iterator.hasNext()) {
      final Sign current = iterator.next();
      final int length = current.length();
      if (pos + length > index)
        return (current instanceof Compound)?
          ((Compound)current).getSignAtIndex(index - pos):
          current;
      else pos += length;
    }
    return null;
  }
  /** Retrieves the visual score object responsible for the braille at index.
   * @see #getSignAtIndex
   */
  public Object getScoreObjectAtIndex(final int index) {
    for (Sign sign = getSignAtIndex(index); sign != null;
         sign = sign.getParent()) {
      final Object object = sign.getScoreObject();
      if (object != null) return object;
    }
    return null;
  }
  public int getIndexOfScoreObject(final Object scoreObject) {
    if (scoreObject == null)
      throw new NullPointerException("Trying to search for null");

    final Iterator<Sign> iterator = iterator();
    int index = 0;
    while (iterator.hasNext()) {
      final Sign current = iterator.next();
      if (current.getScoreObject() == scoreObject) return index;
      if (current instanceof Compound) {
        final Compound compound = (Compound)current;
        final int subIndex = compound.getIndexOfScoreObject(scoreObject);
        if (subIndex >= 0) return index + subIndex;
      }
      index += current.length();
    }

    return -1;
  }
}
