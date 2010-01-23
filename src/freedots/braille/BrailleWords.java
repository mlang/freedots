package freedots.braille;

import freedots.musicxml.Direction;

public class BrailleWords extends BrailleList {
  private final String text;
  private final Direction scoreObject;

  public BrailleWords(String text, Direction direction) {
    super();
    this.text = text;
    this.scoreObject = direction;

    add(new WordSign());
    add(new Text(text));
  }
  @Override public String getDescription() {
    return "A direction that says: " + text;
  }
  @Override public boolean needsGuideDot(BrailleSequence next) {
    if (next.length() > 0) {
      final char ch = next.charAt(0);
      if (((int)ch & 0X2807) > 0X2800) return true;
    }

    return false;
  }
  @Override public Object getScoreObject() { return scoreObject; }
}
