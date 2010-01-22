package freedots.braille;

import freedots.music.AbstractPitch;
import freedots.music.Accidental;
import freedots.music.Articulation;
import freedots.music.Ornament;
import freedots.musicxml.Note;

public class BrailleNote extends BrailleList {
  private Note note;

  public BrailleNote(final Note note, final AbstractPitch lastPitch) {
    super();
    this.note = note;

    if (note.isGrace()) add(new GraceSign());

    for (Ornament ornament: note.getOrnaments())
      add(createOrnamentSign(ornament));
    for (Articulation articulation: note.getArticulations())
      add(createArticulationSign(articulation));

    Accidental accidental = note.getAccidental();
    if (accidental != null) add(new AccidentalSign(accidental));

    AbstractPitch pitch = (AbstractPitch)note.getPitch();
    if (pitch == null)
	pitch = (AbstractPitch)note.getUnpitched();
    if (pitch != null) {
      if (isOctaveSignRequired(pitch, lastPitch))
	add(new OctaveSign(pitch.getOctave()));
      add(new ValueAndPitch(0, 0));
    } else {
       
    }

  }
  @Override public String getDescription() {
    return "A note.";
  }
  @Override public Object getScoreObject() { return note; }

  private static boolean isOctaveSignRequired(final AbstractPitch pitch,
                                              final AbstractPitch lastPitch) {
    if (lastPitch != null) {
      final int halfSteps = Math.abs(pitch.getMIDIPitch()
				     - lastPitch.getMIDIPitch());
      if ((halfSteps < 5)
	  || (halfSteps >= 5 && halfSteps <= 7
	   && pitch.getOctave() == lastPitch.getOctave())) return false;
    }
    return true;
  }

  private static Atom createOrnamentSign(Ornament ornament) {
    switch (ornament) {
    case mordent:         return new MordentSign();
    case invertedMordent: return new InvertedMordentSign();
    case trill:           return new TrillSign();
    case turn:            return new TurnSign();
    default:              throw new AssertionError(ornament);
    }
  }
  public static class MordentSign extends Atom {
    MordentSign() { super(braille(5, 235, 123)); }
    public String getDescription() { return "A mordent sign"; }
  }
  public static class InvertedMordentSign extends Atom {
    InvertedMordentSign() { super(braille(6, 235, 123)); }
    public String getDescription() { return "A inverted mordent sign"; }
  }
  public static class TrillSign extends Atom {
    TrillSign() { super(braille(235)); }
    public String getDescription() { return "A trill sign"; }
  }
  public static class TurnSign extends Atom {
    TurnSign() { super(braille(6, 256)); }
    public String getDescription() { return "A turn sign"; }
  }


  private static Atom createArticulationSign(Articulation articulation) {
    switch (articulation) {
    case accent:        return new AccentSign();
    case strongAccent:  return new MartellatoSign();
    case breathMark:    return new BreathSign();
    case staccato:      return new StaccatoSign();
    case mezzoStaccato: return new MezzoStaccatoSign();
    case staccatissimo: return new StaccatissimoSign();
    case tenuto:        return new TenutoSign();
    default:            throw new AssertionError(articulation);
    }
  }
  public static class AccentSign extends Atom {
    AccentSign() { super(braille(46, 236)); }
    public String getDescription() { return "An accent sign"; }
  }
  public static class MartellatoSign extends Atom {
    MartellatoSign() { super(braille(56, 236)); }
    public String getDescription() {
      return "A martellato (strong accent) sign";
    }
  }
  public static class BreathSign extends Atom {
    BreathSign() { super(braille(6, 34)); }
    public String getDescription() { return "A breath mark"; }
  }
  public static class StaccatoSign extends Atom {
    StaccatoSign() { super(braille(236)); }
    public String getDescription() { return "A staccato sign"; }
  }
  public static class MezzoStaccatoSign extends Atom {
    MezzoStaccatoSign() { super(braille(5, 236)); }
    public String getDescription() { return "A mezzo staccato sign"; }
  }
  public static class StaccatissimoSign extends Atom {
    StaccatissimoSign() { super(braille(6, 236)); }
    public String getDescription() { return "A staccatissimo sign"; }
  }
  public static class TenutoSign extends Atom {
    TenutoSign() { super(braille(456, 236)); }
    public String getDescription() { return "A tenuto sign"; }
  }
}
