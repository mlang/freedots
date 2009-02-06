/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.model;

import java.util.ArrayList;
import java.util.List;

public final class
StaffChord extends AbstractChord<RhythmicElement> implements StaffElement {
  private int staffNumber;

  public StaffChord(RhythmicElement initialNote) {
    super(initialNote);
    this.staffNumber = initialNote.getStaffNumber();
  }

  private Staff staff = null;

  public Staff getStaff() { return staff; }
  public void setStaff(Staff staff) {
    this.staff = staff;
    for (StaffElement element:this) element.setStaff(staff);
  }
  public int getStaffNumber() { return staffNumber; }

  /* FIXME: Seems inappropriate to have to impelement this */
  public boolean isRest() { return false; }

  public List<VoiceElement> getVoiceChords() {
    List<VoiceElement> chords = new ArrayList<VoiceElement>();
    VoiceChord currentVoiceChord = new VoiceChord(get(0));
    chords.add(currentVoiceChord);
    for (int index = 1; index < size(); index++) {
      RhythmicElement note = get(index);
      String noteVoiceName = note.getVoiceName();
      if ((noteVoiceName == null && currentVoiceChord.getVoiceName() == null) ||
          (noteVoiceName != null &&
           noteVoiceName.equals(currentVoiceChord.getVoiceName()))) {
        currentVoiceChord.add(note);
      } else {
        currentVoiceChord = new VoiceChord(note);
        chords.add(currentVoiceChord);
      }
    }
    for (int index = 0; index < chords.size(); index++)
      if (chords.get(index) instanceof VoiceChord) {
        VoiceChord voiceChord = (VoiceChord)chords.get(index);
        if (voiceChord.size() == 1) chords.set(index, voiceChord.get(0));
      }

    return chords;
  }
}
