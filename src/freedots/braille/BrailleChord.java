package freedots.braille;

public class BrailleChord extends BrailleList {
  private Object chord;

  public BrailleChord(int value, int octave, int step, float alter) {
    super();
    chord = new Object();
    add(new AccidentalSign(1));
    add(new OctaveSign(3));
    add(new ValueAndPitch(0, 0));
  }
  @Override public String getDescription() {
    return "A chord.";
  }
  @Override public Object getScoreObject() { return chord; }
}
