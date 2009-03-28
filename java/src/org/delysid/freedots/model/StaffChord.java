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
