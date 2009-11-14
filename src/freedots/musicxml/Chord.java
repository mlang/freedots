/* -*- c-basic-offset: 2; indent-tabs-mode: nil; -*- */
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
package freedots.musicxml;

import java.util.ArrayList;
import java.util.List;

import freedots.music.AbstractChord;
import freedots.music.StaffChord;
import freedots.music.StaffElement;

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
