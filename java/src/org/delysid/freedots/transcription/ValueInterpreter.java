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
          int beginLog = interpretation.get(begin).getLog();
          int endLog = interpretation.get(end).getLog();
          boolean beginLarge = beginLog < 6;
          boolean endLarge = endLog < 6;

          if ((beginLarge && !endLarge) || (!beginLarge && endLarge)) {
            System.err.println("begin is "+beginLarge+" and end is "+endLarge);
            int leftIndex = begin;
            while (interpretation.get(leftIndex).getLog()<6 == beginLarge)
              leftIndex++;
            int rightIndex = end;
            while (interpretation.get(rightIndex).getLog()<6 == endLarge)
              rightIndex--;
            System.err.println("Leftindex is "+leftIndex+" and rightindex is "+rightIndex);
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
        if (rhythmicPossibility.getFraction().compareTo(timeSignature) == 0) {
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
        if (rhythmicPossibility.getFraction().compareTo(timeSignature) < 0) {
          for (Interpretation
               interpretation:
               findInterpretations(tail,
                                   timeSignature.subtract(rhythmicPossibility.getFraction()))) {
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
        if (rhythmicPossibility.getFraction()
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
    public int getLog() { return note.getAugmentedFraction().getLog(); }
    public Fraction getFraction() {
      int log = note.getAugmentedFraction().getLog();
      int dots = note.getAugmentedFraction().getDots();
      if (larger) {
        if (log > 5) log = log - 4;
      } else {
        if (log < 6) log = log + 4;
      }
      AugmentedFraction augmentedFraction = new AugmentedFraction(0, 1, dots);
      augmentedFraction.setFromLog(log);
      return augmentedFraction.basicFraction();
    }
    public String toString() { return getFraction().toString(); }
  }
}
