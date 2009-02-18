package org.delysid.freedots.transcription;

import org.delysid.freedots.Braille;
import org.delysid.freedots.model.AbstractPitch;
import org.delysid.freedots.model.Accidental;
import org.delysid.freedots.musicxml.Note;

class BrailleNote extends BrailleString {
  private AbstractPitch lastPitch;
  BrailleNote(Note note, AbstractPitch lastPitch) {
    super(null, note);
    this.lastPitch = lastPitch;
  }
  AbstractPitch getPitch() {
    Note note = (Note)model;
    return note.getPitch();
  }
  @Override
  public String toString() {
    String braille = "";
    Note note = (Note)model;
    Accidental accidental = note.getAccidental();
    if (accidental != null) {
      braille += accidental.toBraille().toString();
    }
    AbstractPitch pitch = (AbstractPitch)note.getPitch();
    if (pitch != null) {
      Braille octaveSign = pitch.getOctaveSign(lastPitch);
      if (octaveSign != null) { braille += octaveSign; }
    }
    braille += note.getAugmentedFraction().toBrailleString(pitch);

    if (note.isTieStart()) braille += Braille.tie;

    return braille;
  }
}
