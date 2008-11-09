package org.delysid.musicxml;

import java.util.ArrayList;
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
}
