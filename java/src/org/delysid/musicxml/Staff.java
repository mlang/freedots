package org.delysid.musicxml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.delysid.music.Event;

public class Staff {
  String name;
  List<Event> staffElements = new ArrayList<Event>();
  public Staff() {
    this("default");
  }
  public Staff(String name) {
    this.name = name;
  }
  public String getName() { return name; }
  public void add(Event newElement) {
    staffElements.add(newElement);
  }
  public void sort() { Collections.sort(staffElements,
                                        new StaffElementComparator()); }
  class StaffElementComparator implements Comparator<Event> {
    public int compare(Event se1, Event se2) {
	return se1.getOffset().compareTo(se2.getOffset());
    }
  }

  public List<Event> getStaffElements() { return staffElements; }
}
