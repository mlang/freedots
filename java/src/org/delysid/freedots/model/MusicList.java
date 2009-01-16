/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.model;

/* It is never the case that musical objects in the future will affect
 * objects in the past.  This property can be exploited by sorting all
 * the objects by their temporal order into a one-dimensional list.
 */

import java.util.ArrayList;
import java.util.Iterator;
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
  public List<Voice> getVoices() {
    SortedMap<String, Voice> voices = new TreeMap<String, Voice>();
    for (Event event:this) {
      if (event instanceof VoiceElement) {
        String voiceName = ((VoiceElement)event).getVoiceName();
        if (!voices.containsKey(voiceName))
          voices.put(voiceName, new Voice(voiceName));
        voices.get(voiceName).add(event);
      }
    }

    Iterator<Voice> iter = voices.values().iterator();
    while (iter.hasNext()) if (iter.next().restsOnly()) iter.remove();

    return new ArrayList<Voice>(voices.values());    
  }
}
