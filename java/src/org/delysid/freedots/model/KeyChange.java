/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.model;

import org.delysid.freedots.Braille;

public class KeyChange implements StaffElement {
  private Fraction offset;
  private KeySignature keySignature;
  private String staffName = null;
  private Staff staff;

  public KeyChange(Fraction offset, KeySignature keySignature, String staffName) {
    this.offset = offset;
    this.keySignature = keySignature;
    this.staffName = staffName;
  }

  public KeySignature getKeySignature() { return keySignature; }

  public String getStaffName() { return staffName; }

  public Fraction getOffset() { return offset; }

  public Staff getStaff() { return staff; }
  public void setStaff(Staff staff) { this.staff = staff; }
  public boolean isRest() { return false; }
}
