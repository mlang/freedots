package freedots.braille;

import freedots.music.Accidental;

public class BrailleChord extends BrailleList {
  private Object chord;

  public BrailleChord() {
    super();
    chord = new Object();
    add(new AccidentalSign(Accidental.SHARP));
    add(new OctaveSign(3));
    add(new ValueAndPitch(0, 0));
  }
  @Override public String getDescription() {
    return "A chord.";
  }
  @Override public Object getScoreObject() { return chord; }
}
