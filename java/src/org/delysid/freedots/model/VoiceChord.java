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
package org.delysid.freedots.model;

import java.util.Collections;
import java.util.Comparator;

public final class
VoiceChord extends AbstractChord<RhythmicElement> implements VoiceElement {
  private int staffNumber;
  private String voiceName;

  public VoiceChord(RhythmicElement initialNote) {
    super(initialNote);
    this.staffNumber = initialNote.getStaffNumber();
    this.voiceName = initialNote.getVoiceName();
    this.staff = initialNote.getStaff();
  }

  private Staff staff = null;

  public Staff getStaff() { return staff; }
  public void setStaff(Staff staff) { this.staff = staff; }
  public int getStaffNumber() { return staffNumber; }

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
      return n2.getPitch().compareTo(n1.getPitch());
    }
  }

}
