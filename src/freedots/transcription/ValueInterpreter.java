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
package freedots.transcription;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import freedots.music.AugmentedFraction;
import freedots.music.Event;
import freedots.music.Fraction;
import freedots.music.MusicList;
import freedots.music.TimeSignature;
import freedots.musicxml.Note;

/**
 * In braille music there are only four different types of note values.
 * The full range of whole to 128th notes is covered by reusing the same
 * types twice, a whole can also mean a 16th and so on.
 *
 * However, this only works out if there is no ambiguity in the measure
 * to be printed.
 *
 * Class <code>ValueInterpreter</code> calculates the possible interpretations
 * of a list of notes relative to a given time signature.
 */
class ValueInterpreter {
  private Set<Interpretation> interpretations = new HashSet<Interpretation>();

  ValueInterpreter(final MusicList music, final TimeSignature timeSignature) {
    List<Set<RhythmicPossibility>>
    candidates = new ArrayList<Set<RhythmicPossibility>>();
    for (Event event : music) {
      if (event instanceof Note) {
        Note note = (Note)event;
        if (!note.isGrace()) {
          Set<RhythmicPossibility>
          candidate = new HashSet<RhythmicPossibility>(2);
          candidate.add(new Large(note));
          candidate.add(new Small(note));
          candidates.add(candidate);
        }
      } /* FIXME: Handle chords as well */
    }

    if (!candidates.isEmpty()) {
      interpretations = findInterpretations(candidates, timeSignature);
    }
  }

  public Set<Interpretation> getInterpretations() { return interpretations; }

  /** Determines if the simple distrinction of values sign can be used to
   *  resolve a note value ambiguity.
   *
   * @return the note object before which a value distinction sign should be
   *         inserted, or {@code null} if resolution of the value ambiguity is
   *         more complicated.
   * @see freedots.Braille#valueDistinction
   */
  public Object getSplitPoint() {
    for (Interpretation interpretation: interpretations) {
      if (interpretation.isCorrect()) {
        int begin = 0;
        int end = interpretation.size() - 1;

        if (end > begin) {
          boolean beginLarge = isWholeToEighth(interpretation.get(begin));
          boolean endLarge = isWholeToEighth(interpretation.get(end));

          if ((beginLarge && !endLarge) || (!beginLarge && endLarge)) {
            int leftIndex = begin;
            while (isWholeToEighth(interpretation.get(leftIndex)) == beginLarge)
              leftIndex++;
            int rightIndex = end;
            while (isWholeToEighth(interpretation.get(rightIndex)) == endLarge)
              rightIndex--;
            if (rightIndex == leftIndex - 1) {
              return interpretation.get(leftIndex).getNote();
            }
          }
        }
      }
    }
    return null;
  }

  private Set<Interpretation>
  findInterpretations(
    final List<Set<RhythmicPossibility>> candidates, final Fraction remaining
  ) {
    Set<Interpretation> result = new HashSet<Interpretation>();
    if (candidates.size() == 1) {
      for (RhythmicPossibility rhythmicPossibility:candidates.get(0)) {
        if (rhythmicPossibility.equals(remaining)) {
          Interpretation interpretation = new Interpretation();
          interpretation.add(rhythmicPossibility);
          result.add(interpretation);
        }
      }
    } else {
      Set<RhythmicPossibility> head = candidates.get(0);
      List<Set<RhythmicPossibility>>
      tail = candidates.subList(1, candidates.size());
      for (RhythmicPossibility rhythmicPossibility: head) {
        if (rhythmicPossibility.compareTo(remaining) <= 0) {
          for (Interpretation interpretation
                 : findInterpretations(tail, remaining
                                       .subtract(rhythmicPossibility))) {
            interpretation.add(0, rhythmicPossibility);
            result.add(interpretation);
          }
        }
      }
    }

    return result;
  }

  private static boolean isWholeToEighth(RhythmicPossibility item) {
    return item.getLog() < AugmentedFraction.SIXTEENTH;
  }

  @SuppressWarnings("serial")
  class Interpretation extends ArrayList<RhythmicPossibility> {
    Interpretation() { super(); }
    public boolean isCorrect() {
      for (RhythmicPossibility rhythmicPossibility: this)
        if (rhythmicPossibility.isAltered()) return false;

      return true;
    }
    public String toString() {
      StringBuilder sb = new StringBuilder();
      Iterator<RhythmicPossibility> iter = iterator();
      while (iter.hasNext()) {
        sb.append(iter.next().toString());
        if (iter.hasNext()) sb.append(" ");
      }
      return sb.toString();
    }
  }
  abstract class RhythmicPossibility extends AugmentedFraction {
    private Note note;

    RhythmicPossibility(final Note note) {
      super(note.getAugmentedFraction());
      this.note = note;
    }
    boolean isAltered() {
      return compareTo(note.getAugmentedFraction()) != 0;
    }
    Note getNote() { return note; }
  }
  class Large extends RhythmicPossibility {
    Large(final Note note) {
      super(note);
      int log = note.getAugmentedFraction().getLog();
      if (log > EIGHTH) log = log - 4;
      setFromLog(log);
    }
  }
  class Small extends RhythmicPossibility {
    Small(final Note note) {
      super(note);
      int log = note.getAugmentedFraction().getLog();
      if (log < SIXTEENTH) log = log + 4;
      setFromLog(log);
    }
  }
}
