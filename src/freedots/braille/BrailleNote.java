package freedots.braille;

import freedots.music.Accidental;
import freedots.music.AbstractPitch;
import freedots.musicxml.Note;

public class BrailleNote extends BrailleList {
  private Note note;

  public BrailleNote(final Note note, final AbstractPitch lastPitch) {
    super();
    this.note = note;

    if (note.isGrace()) add(new GraceSign());

    Accidental accidental = note.getAccidental();
    if (accidental != null) add(new AccidentalSign(accidental));

    add(new OctaveSign(3));
    add(new ValueAndPitch(0, 0));
  }
  @Override public String getDescription() {
    return "A note.";
  }
  @Override public Object getScoreObject() { return note; }
}
