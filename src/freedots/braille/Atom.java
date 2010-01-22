package freedots.braille;

/** The smallest possible unit of braille.
 * Usually consists of 1 or 2 cells, not much more.
 */
public abstract class Atom implements BrailleSequence {
  protected final String data;
  protected Atom(final String data) { this.data = data; }

  public abstract String getDescription();
  public boolean needsGuideDot(BrailleSequence next) { return false; }

  private BrailleList parent = null;
  public BrailleList getParent() { return parent; }
  public void setParent(final BrailleList parent) { this.parent = parent; }

  public Object getScoreObject() { return null; }

  public String toString() { return data; }
  public int length() { return toString().length(); }
  public char charAt(int index) { return toString().charAt(index); }
  public CharSequence subSequence(int start, int end) {
    return toString().subSequence(start, end);
  }

  protected static String braille(int dots) {
    int bits = 0X2800;
    while (dots > 0) {
      final int number = dots % 10;
      dots /= 10;
      bits |= 1 << (number - 1);
    }
    return String.valueOf((char)bits);
  }
  protected static String braille(int dots1, int dots2) {
    return braille(dots1) + braille(dots2);
  }
  protected static String braille(int dots1, int dots2, int dots3) {
    return braille(dots1) + braille(dots2) + braille(dots3);
  }
}
