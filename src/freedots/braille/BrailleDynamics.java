package freedots.braille;

public class BrailleDynamics extends BrailleList {
  private final String abbrev;

  BrailleDynamics(String abbrev) {
    super();
    this.abbrev = abbrev;

    add(new WordSign());
    add(new Text(abbrev));
  }
  @Override public String getDescription() {
    return "Dynamic indicator: " + abbrev;
  }
  @Override public boolean needsGuideDot(BrailleSequence next) {
    if (next instanceof BrailleDynamics) return false;

    if (next.length() > 0) {
      final char ch = next.charAt(0);
      if (((int)ch & 0X2807) > 0X2800) return true;
    }

    return false;
  }
}
