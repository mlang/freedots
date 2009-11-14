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
package freedots.music;

/* It is never the case that musical objects in the future will affect
 * objects in the past.  This property can be exploited by sorting all
 * the objects by their temporal order into a one-dimensional list.
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import freedots.musicxml.Chord; // FIXME

public class MusicList extends java.util.ArrayList<Event> {
  public MusicList() {
    super();
  }
  public boolean add(Event newElement) {
    int index;
    for (index = 0; index < size(); index++)
      if (get(index).getOffset().compareTo(newElement.getOffset()) > 0) break;

    add(index, newElement);
    return true;
  }

  public int getStaffCount() {
    for (Event event:this) {
      if (event instanceof StartBar) {
        StartBar startBar = (StartBar)event;
        return startBar.getStaffCount();
      }
    }
    return 0;
  }
  public Staff getStaff(int index) {
    List<Staff> staves = new ArrayList<Staff>();

    for (int i = 0; i < getStaffCount(); i++) staves.add(new Staff());

    for (Event event:this) {
      if (event instanceof VerticalEvent) {
        for (Staff staff:staves) staff.add(event);
      } else if (event instanceof StaffElement) {
        staves.get(((StaffElement)event).getStaffNumber()).add(event);
      } else if (event instanceof Chord) {
        for (StaffElement staffChord:((Chord)event).getStaffChords()) {
          int staffNumber = staffChord.getStaffNumber();
          staves.get(staffNumber).add(staffChord);
          staffChord.setStaff(staves.get(staffNumber));
        }
      } else if (event instanceof GlobalKeyChange) {
        GlobalKeyChange globalKeyChange = (GlobalKeyChange)event;
        for (int i = 0; i < staves.size(); i++) {
          KeyChange keyChange = new KeyChange(globalKeyChange.getOffset(),
                                              globalKeyChange.getKeySignature(), i);
          staves.get(i).add(keyChange);
        }
      }
    }
    return staves.get(index);
  }

  public List<Voice> getVoices() {
    SortedMap<String, Voice> voices = new TreeMap<String, Voice>();
    Voice defaultVoice = null;
    for (Event event:this) {
      if (event instanceof VoiceElement) {
        String voiceName = ((VoiceElement)event).getVoiceName();
        if (voiceName == null) {
          if (defaultVoice == null) defaultVoice = new Voice(null);
          defaultVoice.add(event);
        } else {
          if (!voices.containsKey(voiceName))
            voices.put(voiceName, new Voice(voiceName));
          voices.get(voiceName).add(event);
        }
      } else if (event instanceof StaffChord) {
        for (VoiceElement voiceElement:((StaffChord)event).getVoiceChords()) {
          String voiceName = voiceElement.getVoiceName();
          if (voiceName == null) {
            if (defaultVoice == null) defaultVoice = new Voice(null);
            defaultVoice.add(voiceElement);
          } else {
            if (!voices.containsKey(voiceName))
              voices.put(voiceName, new Voice(voiceName));
            voices.get(voiceName).add(voiceElement);
          }
        }
      }
    }

    Iterator<Voice> iter = voices.values().iterator();
    while (iter.hasNext()) if (iter.next().restsOnly()) iter.remove();

    List<Voice> voiceList = new ArrayList<Voice>(voices.values());    
    if (defaultVoice != null) voiceList.add(defaultVoice);
    return voiceList;
  }
  public List<Voice> getVoices(int ordering) {
    List<Voice> voices = getVoices();
    Collections.sort(voices, new VoiceComparator(ordering));
    return voices;
  }
  private class VoiceComparator implements java.util.Comparator<Voice> {
    private int direction;
    VoiceComparator(int direction) {
      this.direction = direction;
    }
    public int compare(Voice v1, Voice v2) {
      int v1Pitch = v1.averagePitch();
      int v2Pitch = v2.averagePitch();
      int result = (v1Pitch < v2Pitch ? -1 : (v1Pitch == v2Pitch ? 0 : 1));
      if (direction > 0) return result;
      return -result;
    }
  } 
  public boolean equalsIgnoreOffset(MusicList other) {
    if (this.size() == other.size()) {
      for (int i = 0; i < size(); i++) {
        if (!(this.get(i).getClass() == other.get(i).getClass())) return false;
        if (!this.get(i).equalsIgnoreOffset(other.get(i))) return false;
      }
      return true;
    }
    return false;
  }
  public String getLyricText() {
    StringBuilder stringBuilder = new StringBuilder();
    for (Event event:this) {
      if (event instanceof freedots.musicxml.Note) {
        freedots.musicxml.Note note = (freedots.musicxml.Note)event;
        Lyric lyric = note.getLyric();
        if (lyric != null) {
          stringBuilder.append(lyric.getText());
          if (lyric.getSyllabic() == Syllabic.SINGLE ||
              lyric.getSyllabic() == Syllabic.END)
            stringBuilder.append(" ");
        }
      }
    }
    return stringBuilder.toString();
  }
  public boolean noteGroupingIsLegal() {
    if (size() > 1 && get(0) instanceof RhythmicElement) {
      RhythmicElement start = (RhythmicElement)get(0);
      AugmentedFraction firstAugmentedFraction = start.getAugmentedFraction();
      for (int index = 1; index < size(); index++) {
        if (get(index) instanceof RhythmicElement) {
          RhythmicElement element = (RhythmicElement)get(index);
          if (element.getPitch() != null)
            if (!firstAugmentedFraction.equals(element.getAugmentedFraction()))
              return false;
          else return false;
        } else return false;
      }
    }
    return false;
  }
}
