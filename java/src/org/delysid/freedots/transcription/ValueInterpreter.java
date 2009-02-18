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

class ValueInterpreter {
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
      }
    }
    Set<Interpretation>
    interpretations = findInterpretations(candidates, timeSignature);
  }
  public Set<Interpretation>
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
      candidates.remove(head);
      ArrayList<Set<RhythmicPossibility>> tail = candidates;
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
  }
  class RhythmicPossibility {
    Note note;
    boolean larger;
    RhythmicPossibility(Note note, boolean larger) {
      this.note = note;
      this.larger = larger;
    }
    public AugmentedFraction getAugmentedFraction() {
      AugmentedFraction augmentedFraction = note.getAugmentedFraction();
      int log = Util.log2(augmentedFraction.getDenominator());
      if (larger) {
        if (log > 3) log = log - 4;
      } else {
        if (log < 4) log = log + 4;
      }
      augmentedFraction.setDenominator((int)Math.round(Math.pow(2, log)));
      return augmentedFraction;
    }
  }
}
