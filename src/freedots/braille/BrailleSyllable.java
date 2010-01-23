package freedots.braille;

import freedots.musicxml.Note;

public class BrailleSyllable extends Text {
  private final Note note;

  public BrailleSyllable(final String text, final Note note) {
    super(text);
    this.note = note;
  }
  @Override public String getDescription() {
    return "The syllable \"" + data + "\"";
  }
  @Override public Object getScoreObject() { return note; }
}
