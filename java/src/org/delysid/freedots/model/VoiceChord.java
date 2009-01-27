/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.model;

import java.util.Collections;
import java.util.Comparator;

public final class
VoiceChord extends AbstractChord<RhythmicElement> implements VoiceElement {
  private String staffName;
  private String voiceName;

  public VoiceChord(RhythmicElement initialNote) {
    super(initialNote);
    this.staffName = initialNote.getStaffName();
    this.voiceName = initialNote.getVoiceName();
    this.staff = initialNote.getStaff();
  }

  private Staff staff = null;

  public Staff getStaff() { return staff; }
  public void setStaff(Staff staff) { this.staff = staff; }
  public String getStaffName() { return staffName; }

  /* FIXME: Seems inappropriate to have to impelement this */
  public boolean isRest() { return false; }

  public String getVoiceName() { return voiceName; }
  public void setVoiceName(String voiceName) {
    this.voiceName = voiceName;
    for (VoiceElement element:this) { element.setVoiceName(voiceName); }
  }

  public VoiceChord getSorted() {
    VoiceChord newChord = (VoiceChord)this.clone();
    Collections.sort(newChord, getStaff().getClef(offset).getChordDirection() > 0?
                                 new AscendingNoteComparator():
                                 new DescendingNoteComparator());
    return newChord;
  }
  private class AscendingNoteComparator implements Comparator<RhythmicElement> {
    public int compare(RhythmicElement n1, RhythmicElement n2) {
      return n1.getPitch().compareTo(n2.getPitch());
    }
  }
  private class DescendingNoteComparator implements Comparator<RhythmicElement> {
    public int compare(RhythmicElement n1, RhythmicElement n2) {
      return -n1.getPitch().compareTo(n2.getPitch());
    }
  }
}
