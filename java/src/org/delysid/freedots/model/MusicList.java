/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.model;

/* It is never the case that musical objects in the future will affect
 * objects in the past.  This property can be exploited by sorting all
 * the objects by their temporal order into a one-dimensional list.
 */

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

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
  public List<MusicList> getVoices() {
    SortedMap<String, MusicList> voices = new TreeMap<String, MusicList>();
    for (Event event:this) {
      if (event instanceof VoiceElement) {
        String voiceName = ((VoiceElement)event).getVoiceName();
        if (!voices.containsKey(voiceName))
          voices.put(voiceName, new MusicList());
        voices.get(voiceName).add(event);
      }
    }
    return new ArrayList<MusicList>(voices.values());    
  }
}
