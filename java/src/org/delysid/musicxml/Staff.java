package org.delysid.musicxml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Staff {
  String name;
  List<StaffElement> staffElements = new ArrayList<StaffElement>();
  public Staff() {
    this("default");
  }
  public Staff(String name) {
    this.name = name;
  }
  public String getName() { return name; }
  public void add(StaffElement newElement) {
    staffElements.add(newElement);
  }
  public void sort() { Collections.sort(staffElements,
                                        new StaffElementComparator()); }
  class StaffElementComparator implements Comparator<StaffElement> {
    public int compare(StaffElement se1, StaffElement se2) {
      return se1.getOffset() - se2.getOffset();
    }
  }
}
