package org.delysid.freedots.transcription;

import java.util.Set;

import org.delysid.freedots.Braille;
import org.delysid.freedots.model.AbstractPitch;
import org.delysid.freedots.model.Accidental;
import org.delysid.freedots.model.Fingering;
import org.delysid.freedots.model.Ornament;
import org.delysid.freedots.model.Slur;
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

    if (note.isGrace()) {
      braille += Braille.grace;
    }

    Set<Ornament> ornaments = note.getOrnaments();
    if (ornaments != null) {
      for (Ornament ornament:note.getOrnaments())
        braille += ornament.toBraille();
    }

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

    Fingering fingering = note.getFingering();
    if (fingering != null) {
      braille += fingering.toBrailleString();
    }
    if (note.isTieStart()) {
      braille += Braille.tie;
    } else {
      boolean printSlur = false;
      for (Slur slur:note.getSlurs()) {
        if (!slur.lastNote(note)) {
          printSlur = true;
          break;
        }
      }
      if (printSlur) {
        braille += Braille.slur;
      }
    }

    return braille;
  }
}
