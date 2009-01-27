/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.model;

public class ClefChange implements Event {
  private Fraction offset;
  private Clef clef;
  private String staffName = null;

  public ClefChange(Fraction offset, Clef clef, String staffName) {
    this.offset = offset;
    this.clef = clef;
    this.staffName = staffName;
  }

  public Clef getClef() { return clef; }

  public String getStaffName() { return staffName; }

  public Fraction getOffset() { return offset; }
}

