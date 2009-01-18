/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.model;

public class Staff extends MusicList {
  String name;
  public Staff() { super(); }

  public void setName(String name) { this.name = name; }

  public boolean add(Event event) {
    if (super.add(event)) {
      if (event instanceof StaffElement) ((StaffElement)event).setStaff(this);
      return true;
    }
    return false;
  }
}

