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
    return super.needsGuideDot(next);
  }
}
