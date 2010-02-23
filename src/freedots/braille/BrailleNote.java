/* -*- c-basic-offset: 2; indent-tabs-mode: nil; -*- */
/*
 * FreeDots -- MusicXML to braille music transcription
 *
 * Copyright 2008-2010 Mario Lang  All Rights Reserved.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 3, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details (a copy is included in the LICENSE.txt file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License
 * along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This file is maintained by Mario Lang <mlang@delysid.org>.
 */
package freedots.braille;

import freedots.Options;
import freedots.music.AbstractPitch;
import freedots.music.Accidental;
import freedots.music.Articulation;
import freedots.music.AugmentedPowerOfTwo;
import freedots.music.Fingering;
import freedots.music.Ornament;
import freedots.music.Slur;
import freedots.musicxml.Note;

/** The braille representation of a note or rest.
 * <p>
 * This includes all signs which must immediately follow or proceed the actual
 * value or rest sign.
 *
 * @see <a href="http://brl.org/music/code/bmb/chap01/index.html">Chapter 1:
 *      Notes and Values</a>
 */
public class BrailleNote extends BrailleList {
  private final Note note;

  /** Constructs a braille note and all of its composing signs.
   * @param note refers to the MusicXML Note object
   * @param lastPitch is used to decide if an octave sign needs to be inserted
   */
  public BrailleNote(final Note note, final AbstractPitch lastPitch) {
    this(note, lastPitch, true);
  }
  /** Constructs a braille note and all its composing signs.
   * @param note refers to the MusicXML Note object
   * @param lastPitch is used to decide if an octave sign needs to be inserted
   * @param allowTieSign is false if ties should not be inserted.  This is
   *        used to support {@link BrailleChord.ChordTieSign}.
   */
  BrailleNote(final Note note, final AbstractPitch lastPitch,
              final boolean allowTieSign) {
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
    if (pitch == null) /* A hack to support unpitched notes */
      pitch = (AbstractPitch)note.getUnpitched();

    final AugmentedPowerOfTwo value = note.getAugmentedFraction();

    if (pitch != null) { /* A sounding note */
      if (isOctaveSignRequired(pitch, lastPitch))
        add(new OctaveSign(pitch.getOctave()));

      add(new PitchAndValueSign(pitch, value));
    } else {
      add(new RestSign(value));
    }
    for (int i = 0; i < value.dots(); i++) add(new Dot());

    if (Options.getInstance().getShowFingering()) {
      final Fingering fingering = note.getFingering();
      if (!fingering.getFingers().isEmpty()) {
        add(new BrailleFingering(fingering));
      }
    }

    boolean addSlur = false;
    for (Slur<Note> slur:note.getSlurs()) {
      if (!slur.lastNote(note)) {
        addSlur = true;
        break;
      }
    }
    if (addSlur) add(new SlurSign());

    if (allowTieSign && note.isTieStart()) add(new TieSign());
  }
  @Override public String getDescription() {
    return "A note.";
  }
  @Override public Object getScoreObject() { return note; }

  public AbstractPitch getPitch() { return note.getPitch(); }

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

  public static class GraceSign extends Sign {
    GraceSign() { super(braille(5, 26)); }
    public String getDescription() {
      return "Indicates the this is a grace note";
    }
  }

  private static OrnamentSign createOrnamentSign(Ornament ornament) {
    switch (ornament) {
    case mordent:         return new MordentSign();
    case invertedMordent: return new InvertedMordentSign();
    case trill:           return new TrillSign();
    case turn:            return new TurnSign();
    default:              throw new AssertionError(ornament);
    }
  }
  public abstract static class OrnamentSign extends Sign {
    OrnamentSign(final String data) { super(data); }
  }
  public static class MordentSign extends OrnamentSign {
    MordentSign() { super(braille(5, 235, 123)); }
    public String getDescription() { return "A mordent sign"; }
  }
  public static class InvertedMordentSign extends OrnamentSign {
    InvertedMordentSign() { super(braille(6, 235, 123)); }
    public String getDescription() { return "A inverted mordent sign"; }
  }
  public static class TrillSign extends OrnamentSign {
    TrillSign() { super(braille(235)); }
    public String getDescription() { return "A trill sign"; }
  }
  public static class TurnSign extends OrnamentSign {
    TurnSign() { super(braille(6, 256)); }
    public String getDescription() { return "A turn sign"; }
  }

  private static ArticulationSign createArticulationSign(Articulation articulation) {
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
  public abstract static class ArticulationSign extends Sign {
    ArticulationSign(final String data) { super(data); }
  }
  public static class AccentSign extends ArticulationSign {
    AccentSign() { super(braille(46, 236)); }
    public String getDescription() { return "An accent sign"; }
  }
  public static class MartellatoSign extends ArticulationSign {
    MartellatoSign() { super(braille(56, 236)); }
    public String getDescription() {
      return "A martellato (strong accent) sign";
    }
  }
  public static class BreathSign extends ArticulationSign {
    BreathSign() { super(braille(6, 34)); }
    public String getDescription() { return "A breath mark"; }
  }
  public static class StaccatoSign extends ArticulationSign {
    StaccatoSign() { super(braille(236)); }
    public String getDescription() { return "A staccato sign"; }
  }
  public static class MezzoStaccatoSign extends ArticulationSign {
    MezzoStaccatoSign() { super(braille(5, 236)); }
    public String getDescription() { return "A mezzo staccato sign"; }
  }
  public static class StaccatissimoSign extends ArticulationSign {
    StaccatissimoSign() { super(braille(6, 236)); }
    public String getDescription() { return "A staccatissimo sign"; }
  }
  public static class TenutoSign extends ArticulationSign {
    TenutoSign() { super(braille(456, 236)); }
    public String getDescription() { return "A tenuto sign"; }
  }
}
