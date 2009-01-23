package org.delysid.freedots.musicxml;

import java.util.ArrayList;
import java.util.List;

import org.delysid.freedots.model.AbstractChord;
import org.delysid.freedots.model.StaffChord;

public class Chord extends AbstractChord<Note> {
  Chord(Note initialNote) {
    super(initialNote);
  }
  public List<StaffChord> getStaffChords() {
    List<StaffChord> chords = new ArrayList<StaffChord>();
    StaffChord currentStaffChord = new StaffChord(get(0));
    chords.add(currentStaffChord);
    for (int index = 1; index < size(); index++) {
      Note note = get(index);
      if (note.getStaffName().equals(currentStaffChord.getStaffName())) {
        currentStaffChord.add(note);
      } else {
        currentStaffChord = new StaffChord(note);
        chords.add(currentStaffChord);
      }
    }
    return chords;
  }
}
