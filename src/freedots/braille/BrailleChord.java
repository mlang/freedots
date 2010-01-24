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
  private final BrailleNote topNote;

  public BrailleChord(final VoiceChord chord, final AbstractPitch lastPitch) {
    super();
    this.chord = chord;

    final Iterator<RhythmicElement> iterator = chord.getSorted().iterator();
    assert iterator.hasNext();

    final Note firstNote = (Note)iterator.next();
    topNote = new BrailleNote(firstNote, lastPitch);
    add(topNote);

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

  public AbstractPitch getNotePitch() { return topNote.getPitch(); }

  public static class ChordStep extends BrailleList {
    private final Note note;

    ChordStep(final Note note, final Note relativeTo) {
      super();
      this.note = note;

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
    }

    @Override public Object getScoreObject() { return note; }
  }
  public static class IntervalSign extends Atom {
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
}
