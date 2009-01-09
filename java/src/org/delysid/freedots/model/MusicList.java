/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.model;

/* It is never the case that musical objects in the future will affect
 * objects in the past.  This property can be exploited by sorting all
 * the objects by their temporal order into a one-dimensional list.
 */
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
}
