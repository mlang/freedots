/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.musicxml;

import java.util.ArrayList;
import java.util.List;

import org.delysid.freedots.model.AbstractChord;
import org.delysid.freedots.model.StaffChord;
import org.delysid.freedots.model.StaffElement;

public final class Chord extends AbstractChord<Note> {
  Chord(Note initialNote) {
    super(initialNote);
  }
  public List<StaffElement> getStaffChords() {
    List<StaffElement> chords = new ArrayList<StaffElement>();
    StaffChord currentStaffChord = new StaffChord(get(0));
    chords.add(currentStaffChord);
    for (int index = 1; index < size(); index++) {
      Note note = get(index);
      int noteStaffNumber = note.getStaffNumber();
      if (noteStaffNumber == currentStaffChord.getStaffNumber()) {
        currentStaffChord.add(note);
      } else {
        currentStaffChord = new StaffChord(note);
        chords.add(currentStaffChord);
      }
    }
    for (int index = 0; index < chords.size(); index++) {
      if (chords.get(index) instanceof StaffChord) {
        StaffChord staffChord = (StaffChord)chords.get(index);
        if (staffChord.size() == 1) chords.set(index, staffChord.get(0));
      }
    }

    return chords;
  }
}
