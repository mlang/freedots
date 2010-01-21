package freedots.braille;

public class BrailleNote extends Compound {
  private Object note;

  public BrailleNote(int value, int octave, int step, float alter) {
    super();
    note = new Object();
    add(new AccidentalSign(1));
    add(new OctaveSign(3));
    add(new ValueAndPitch(0, 0));
  }
  @Override public String getDescription() {
    return "A note.";
  }
  @Override public Object getScoreObject() { return note; }
}
