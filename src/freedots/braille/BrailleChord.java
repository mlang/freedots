package freedots.braille;

import java.util.Iterator;

import freedots.Options;
import freedots.music.AbstractPitch;
import freedots.music.Accidental;
import freedots.music.Fingering;
import freedots.music.RhythmicElement;
import freedots.music.VoiceChord;
import freedots.musicxml.Note;

public class BrailleChord extends BrailleList {
  private final VoiceChord chord;
  private final Note firstNote;

  public BrailleChord(final VoiceChord chord, final AbstractPitch lastPitch) {
    super();
    this.chord = chord;

    final Iterator<RhythmicElement> iterator = chord.getSorted().iterator();
    assert iterator.hasNext();

    firstNote = (Note)iterator.next();
    add(new BrailleNote(firstNote, lastPitch));

    assert iterator.hasNext();

    while (iterator.hasNext()) {
      final Note currentNote = (Note)iterator.next();
      final Accidental accidental = currentNote.getAccidental();
      if (accidental != null) add(new AccidentalSign(accidental));
      AbstractPitch currentPitch = (AbstractPitch)currentNote.getPitch();
      if (currentPitch == null)
	currentPitch = (AbstractPitch)currentNote.getUnpitched();
      add(new ChordStep(currentNote, firstNote));

      if (Options.getInstance().getShowFingering()) {
	final Fingering fingering = currentNote.getFingering();
	if (!fingering.getFingers().isEmpty())
	  add(new BrailleFingering(fingering));
      }

      if (currentNote.isTieStart()) {
        add(new TieSign());
      }
    }
  }
  @Override public String getDescription() {
    return "A chord.";
  }
  @Override public Object getScoreObject() { return chord; }

  public static class ChordStep extends BrailleList {
    private final Note note;

    ChordStep(final Note note, final Note relativeTo) {
      super();
      this.note = note;

      AbstractPitch thisPitch = note.getPitch();
      if (thisPitch == null) thisPitch = note.getUnpitched();
      AbstractPitch otherPitch = relativeTo.getPitch();
      if (thisPitch == null) otherPitch = relativeTo.getUnpitched();

      int diatonicDiff = Math.abs(thisPitch.diatonicDifference(otherPitch));
      if (diatonicDiff == 0 || diatonicDiff > 7) {
        add(new OctaveSign(thisPitch.getOctave()));
	while (diatonicDiff > 7) diatonicDiff -= 7;
      }
      add(new Interval(diatonicDiff));
    }

    @Override public Object getScoreObject() { return note; }
  }
  public static class Interval extends Atom {
    private final int steps;
    Interval(final int steps) {
      super(INTERVALS[steps]);
      this.steps = steps;
    }

    public String getDescription() {
      return "A " + INTERVAL_NAMES[steps] + " interval sign";
    }

    private static final String[] INTERVALS = new String[] {
      braille(36), braille(34), braille(346), braille(3456), braille(35),
      braille(356), braille(25)
    };
    private static final String[] INTERVAL_NAMES = new String[] {
      "octave/unison", "second", "third", "fourth", "fifth", "sixth",
      "seventh"
    };
  }
}
