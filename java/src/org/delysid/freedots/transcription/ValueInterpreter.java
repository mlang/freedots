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
import org.delysid.freedots.Util;

/**
 * In braille music there are only 4 different types of note values.
 * The full range of whole to 128th notes is covered by reusing the same
 * types twice, a whole can also mean a 16th and so on.
 *
 * However, this only works out if there is no ambiguity in the measure
 * to be rendered.
 *
 * Class <code>ValueInterpreter</code> calculates the possible interpretations
 * of a list of notes relative to a given time signature.
 */
class ValueInterpreter {
  private Set<Interpretation> interpretations;

  ValueInterpreter(MusicList music, TimeSignature timeSignature) {
    ArrayList<Set<RhythmicPossibility>>
    candidates = new ArrayList<Set<RhythmicPossibility>>();
    for (Event event:music) {
      if (event instanceof Note) {
        Note note = (Note)event;
        RhythmicPossibility large = new RhythmicPossibility(note, true);
        RhythmicPossibility small = new RhythmicPossibility(note, false);
        Set<RhythmicPossibility> candidate = new HashSet<RhythmicPossibility>();
        candidate.add(large);
        candidate.add(small);
        candidates.add(candidate);
      } /* FIXME: Handle chords as well */
    }
    interpretations = findInterpretations(candidates, timeSignature);
  }
  public Set<Interpretation> getInterpretations() { return interpretations; }

  private Set<Interpretation>
  findInterpretations(
    ArrayList<Set<RhythmicPossibility>> candidates, Fraction timeSignature
  ) {
    Set<Interpretation> result = new HashSet<Interpretation>();
    if (candidates.size() == 1) {
      for (RhythmicPossibility rhythmicPossibility:candidates.get(0)) {
        if (rhythmicPossibility.getAugmentedFraction().compareTo(timeSignature) == 0) {
          Interpretation interpretation = new Interpretation();
          interpretation.add(rhythmicPossibility);
          result.add(interpretation);
        }
      }
    } else {
      Set<RhythmicPossibility> head = candidates.get(0);
      ArrayList<Set<RhythmicPossibility>> tail = (ArrayList<Set<RhythmicPossibility>>)candidates.clone();
      tail.remove(head);
      for (RhythmicPossibility rhythmicPossibility:head) {
        if (rhythmicPossibility.getAugmentedFraction().compareTo(timeSignature) < 0) {
          for (Interpretation interpretation:findInterpretations(tail, timeSignature.subtract(rhythmicPossibility.getAugmentedFraction()))) {
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
    public String toString() {
      StringBuilder sb = new StringBuilder();
      for (RhythmicPossibility rp:this) {
        sb.append(rp.toString());
        sb.append(" ");
      }
      return sb.toString();
    }
  }
  class RhythmicPossibility {
    Note note;
    boolean larger;
    RhythmicPossibility(Note note, boolean larger) {
      this.note = note;
      this.larger = larger;
    }
    public Fraction getAugmentedFraction() {
      AugmentedFraction augmentedFraction = note.getAugmentedFraction();
      int log = Util.log2(augmentedFraction.getDenominator());
      if (larger) {
        if (log > 3) log = log - 4;
      } else {
        if (log < 4) log = log + 4;
      }
      augmentedFraction.setDenominator((int)Math.round(Math.pow(2, log)));
      return augmentedFraction.basicFraction();
    }
    public String toString() { return getAugmentedFraction().toString(); }
  }
}
