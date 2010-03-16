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

import java.util.Comparator;
import java.util.Iterator;

import freedots.Options;
import freedots.music.AbstractPitch;
import freedots.music.Accidental;
import freedots.music.Fingering;
import freedots.music.RhythmicElement;
import freedots.music.Slur;
import freedots.music.VoiceChord;
import freedots.musicxml.Note;

/** Represents several notes with same duration and start time, i.e., a chord.
 */
public class BrailleChord extends BrailleList {
  private final VoiceChord chord;
  private final BrailleNote topNote;

  /** Constructs the braille representation of the given chord.
   * @param chord is essentially a list of notes
   * @param lastPitch is used to decide if an {@link OctaveSign octave sign}
   *        should be printed.  It should be {@code null} if an octave sign
   *        should be printed regardless of the previous pitch.
   */
  public BrailleChord(final VoiceChord chord,
                      final Comparator<RhythmicElement> comparator,
                      final AbstractPitch lastPitch) {
    super();
    this.chord = chord;

    final VoiceChord sorted = (VoiceChord)chord.clone();
    java.util.Collections.sort(sorted, comparator);
    final Iterator<RhythmicElement> iterator = sorted.iterator();
    assert iterator.hasNext();

    final boolean allNotesTied = hasAllNotesTied();

    final Note firstNote = (Note)iterator.next();
    add(topNote = new BrailleNote(firstNote, lastPitch, !allNotesTied));

    assert iterator.hasNext();

    while (iterator.hasNext()) {
      add(new ChordStep((Note)iterator.next(), firstNote, !allNotesTied));
    }

    if (allNotesTied) add(new ChordTieSign());
  }
  @Override public String getDescription() {
    return "A chord.";
  }
  @Override public Object getScoreObject() { return chord; }

  /** Returns the pitch of the first note of this chord.
   * <p>
   * The first note is chord direction dependant, if the chord is written
   * from bottom to top note, this method returns the lowest pitch of the
   * chord.
   */
  public AbstractPitch getNotePitch() { return topNote.getPitch(); }

  private boolean hasAllNotesTied() {
    final Iterator<RhythmicElement> iterator = chord.iterator();
    while (iterator.hasNext()) {
      if (!((Note)iterator.next()).isTieStart()) return false;
    }
    return true;
  }

  /** Represents an interval in the braille chord.
   */
  public static class ChordStep extends BrailleList {
    private final Note note;

    ChordStep(final Note note, final Note relativeTo, final boolean allowTieSign) {
      super();
      this.note = note;

      final Options options = Options.getInstance();

      final Accidental accidental = note.getAccidental();
      if (accidental != null) add(new AccidentalSign(accidental));

      AbstractPitch thisPitch = note.getPitch();
      if (thisPitch == null) thisPitch = note.getUnpitched();
      AbstractPitch otherPitch = relativeTo.getPitch();
      if (otherPitch == null) otherPitch = relativeTo.getUnpitched();

      int diatonicDiff = Math.abs(thisPitch.diatonicDifference(otherPitch));
      if (diatonicDiff == 0 || diatonicDiff > 7) {
        add(new OctaveSign(thisPitch.getOctave()));
        while (diatonicDiff > 7) diatonicDiff -= 7;
      }
      add(new IntervalSign(diatonicDiff));

      if (options.getShowFingering()) {
        final Fingering fingering = note.getFingering();
        if (!fingering.getFingers().isEmpty())
          add(new BrailleFingering(fingering));
      }

      boolean addSingleSlur = false;
      boolean addDoubledSlur = false;
      for (Slur<Note> slur: note.getSlurs()) {
        if (slur.countArcs(note) >= options.getSlurDoublingThreshold()) {
          if (slur.isFirst(note)) {
            addDoubledSlur = true; addSingleSlur = false;
          } else if (slur.isLastArc(note)) {
            addDoubledSlur = false; addSingleSlur = true;
          } else {
            addDoubledSlur = false; addSingleSlur = false;
          }
          break;
        } else {
          if (!slur.lastNote(note)) {
            addDoubledSlur = false; addSingleSlur = true;
            break;
          }
        }
      }
      if (addDoubledSlur) {
        add(new SlurSign()); add(new SlurSign());
      } else if (addSingleSlur) {
        add(new SlurSign());
      }

      if (allowTieSign && note.isTieStart()) { add(new TieSign()); }
    }

    /** Returns the target note indicated by this interval.
     */
    @Override public Object getScoreObject() { return note; }
  }

  /** Represents an interval.
   */
  public static class IntervalSign extends Sign {
    private final int steps;
    IntervalSign(final int steps) {
      super(INTERVALS[steps]);
      this.steps = steps;
    }

    public String getDescription() {
      return "A " + INTERVAL_NAMES[steps] + " interval sign";
    }

    private static final String[] INTERVALS = new String[] {
      braille(36), braille(34), braille(346), braille(3456), braille(35),
      braille(356), braille(25), braille(36)
    };
    private static final String[] INTERVAL_NAMES = new String[] {
      "unison", "second", "third", "fourth", "fifth", "sixth",
      "seventh", "octave"
    };
  }

  public static class ChordTieSign extends Sign {
    ChordTieSign() { super(braille(46, 14)); }
    public String getDescription() {
      return "Indicates that all notes of a chord are tied to the next chord.";
    }
  }
}

