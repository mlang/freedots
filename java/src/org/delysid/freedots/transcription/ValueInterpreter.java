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
        RhythmicPossibility large = new RhythmicPossibility(note, true);
        RhythmicPossibility small = new RhythmicPossibility(note, false);
        Set<RhythmicPossibility> candidate = new HashSet<RhythmicPossibility>();
        candidate.add(large);
        candidate.add(small);
        candidates.add(candidate);
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
          int beginLog = Util.log2(interpretation.get(begin).getAugmentedFraction().getDenominator());        
          int endLog = Util.log2(interpretation.get(end).getAugmentedFraction().getDenominator());
          boolean beginLarge = beginLog < 4;
          boolean endLarge = endLog < 4;

          if ((beginLarge && !endLarge) || (!beginLarge && endLarge)) {
            int leftIndex = begin;
            while (Util.log2(interpretation.get(leftIndex).getAugmentedFraction().getDenominator())<4 == beginLarge)
              leftIndex++;
            int rightIndex = end;
            while (Util.log2(interpretation.get(rightIndex).getAugmentedFraction().getDenominator())<4 == endLarge)
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
    List<Set<RhythmicPossibility>> candidates, Fraction timeSignature
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
      List<Set<RhythmicPossibility>>
      tail = candidates.subList(1, candidates.size());
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
    public boolean isCorrect() {
      for (RhythmicPossibility rhythmicPossibility:this) {
        if (rhythmicPossibility.getAugmentedFraction()
            .compareTo(rhythmicPossibility.getNote().getAugmentedFraction()) != 0)
          return false;
      }
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
  class RhythmicPossibility {
    private Note note;
    private boolean larger;

    RhythmicPossibility(Note note, boolean larger) {
      this.note = note;
      this.larger = larger;
    }
    public Note getNote() { return note; }
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
