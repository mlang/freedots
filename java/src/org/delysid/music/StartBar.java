/* -*- c-basic-offset: 2; -*- */
package org.delysid.music;

import org.delysid.Fraction;

public class StartBar extends VerticalEvent {
  public StartBar(Fraction offset) { super(offset); }

  int staffCount;
  public int getStaffCount() { return staffCount; }
  public void setStaffCount(int staffCount) { this.staffCount = staffCount; }
}
