/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.model;

import org.delysid.freedots.Fraction;

public class StartBar extends VerticalEvent {
  public StartBar(Fraction offset) { super(offset); }

  int staffCount;
  public int getStaffCount() { return staffCount; }
  public void setStaffCount(int staffCount) { this.staffCount = staffCount; }

  boolean newSystem = false;
  public boolean getNewSystem() { return newSystem; }
  public void setNewSystem(boolean newSystem) { this.newSystem = newSystem; }
}
