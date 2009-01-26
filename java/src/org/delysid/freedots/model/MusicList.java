/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.model;

/* It is never the case that musical objects in the future will affect
 * objects in the past.  This property can be exploited by sorting all
 * the objects by their temporal order into a one-dimensional list.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.delysid.freedots.musicxml.Chord; // FIXME

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
    Map<String, Staff> staffNames = new HashMap<String, Staff>();
    int usedStaves = 0;

    for (int i = 0; i < getStaffCount(); i++) staves.add(new Staff());

    for (Event event:this) {
      if (event instanceof VerticalEvent) {
        for (Staff staff:staves) staff.add(event);
      } else if (event instanceof StaffElement) {
        String staffName = ((StaffElement)event).getStaffName();
        if (!staffNames.containsKey(staffName))
          staffNames.put(staffName, staves.get(usedStaves++));
        staffNames.get(staffName).add(event);
      } else if (event instanceof Chord) {
        for (StaffElement staffChord:((Chord)event).getStaffChords()) {
          String staffName = staffChord.getStaffName();
          if (!staffNames.containsKey(staffName))
            staffNames.put(staffName, staves.get(usedStaves++));
          staffNames.get(staffName).add(staffChord);
          staffChord.setStaff(staffNames.get(staffName));
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
}
