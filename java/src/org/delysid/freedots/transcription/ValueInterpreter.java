/* -*- c-basic-offset: 2; -*- */
/*
 * FreeDots -- MusicXML to braille music transcription
 *
 * Copyright 2008-2009 Mario Lang  All Rights Reserved.
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
package org.delysid.freedots.transcription;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.delysid.freedots.model.AugmentedFraction;
import org.delysid.freedots.model.Event;
import org.delysid.freedots.model.Fraction;
import org.delysid.freedots.model.MusicList;
import org.delysid.freedots.model.TimeSignature;
import org.delysid.freedots.musicxml.Note;

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

  ValueInterpreter(MusicList music, TimeSignature timeSignature) {
    List<Set<RhythmicPossibility>>
    candidates = new ArrayList<Set<RhythmicPossibility>>();
    for (Event event:music) {
      if (event instanceof Note) {
        Note note = (Note)event;
        if (!note.isGrace()) {
          RhythmicPossibility large = new RhythmicPossibility(note, true);
          RhythmicPossibility small = new RhythmicPossibility(note, false);
          Set<RhythmicPossibility> candidate = new HashSet<RhythmicPossibility>();
          candidate.add(large);
          candidate.add(small);
          candidates.add(candidate);
        }
      } /* FIXME: Handle chords as well */
    }

    if (!candidates.isEmpty()) {
      interpretations = findInterpretations(candidates, timeSignature);
    }
  }

  public Set<Interpretation> getInterpretations() { return interpretations; }

  public Object getSplitPoint() {
    for (Interpretation interpretation:interpretations) {
      if (interpretation.isCorrect()) {
        int begin = 0;
        int end = interpretation.size() - 1;

        if (end > begin) {
          int beginLog = interpretation.get(begin).getLog();
          int endLog = interpretation.get(end).getLog();
          boolean beginLarge = beginLog < 6;
          boolean endLarge = endLog < 6;

          if ((beginLarge && !endLarge) || (!beginLarge && endLarge)) {
            int leftIndex = begin;
            while (interpretation.get(leftIndex).getLog()<6 == beginLarge)
              leftIndex++;
            int rightIndex = end;
            while (interpretation.get(rightIndex).getLog()<6 == endLarge)
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
    List<Set<RhythmicPossibility>> candidates, Fraction remaining
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
      for (RhythmicPossibility rhythmicPossibility:head) {
        if (rhythmicPossibility.compareTo(remaining) <= 0) {
          for (Interpretation interpretation :
               findInterpretations(tail,
                                   remaining.subtract(rhythmicPossibility))) {
            interpretation.add(0, rhythmicPossibility);
            result.add(interpretation);
          }
        }
      }
    }

    return result;
  }

  class Interpretation extends ArrayList<RhythmicPossibility> {
    Interpretation() {super();}
    public boolean isCorrect() {
      for (RhythmicPossibility rhythmicPossibility:this)
        if (rhythmicPossibility.isAltered()) return false;

      return true;
    }
    public String toString() {
      StringBuilder sb = new StringBuilder();
      for (RhythmicPossibility rp:this) {
        sb.append(rp.toString());
        sb.append(" ");
      }
      return sb.toString();
    }
  }
  class RhythmicPossibility extends AugmentedFraction {
    private Note note;

    RhythmicPossibility(Note note, boolean larger) {
      super(note.getAugmentedFraction());
      this.note = note;
      int log = note.getAugmentedFraction().getLog();
      if (larger) {
        if (log > 5) log = log - 4;
      } else {
        if (log < 6) log = log + 4;
      }
      setFromLog(log);
    }
    public boolean isAltered() {
      return compareTo(note.getAugmentedFraction()) != 0;
    }
    public Note getNote() { return note; }
  }
}
