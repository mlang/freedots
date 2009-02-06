/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.model;

public class KeyChange implements StaffElement {
  private Fraction offset;
  private KeySignature keySignature;
  private int staffNumber;
  private Staff staff;

  public KeyChange(Fraction offset, KeySignature keySignature, int staffNumber) {
    this.offset = offset;
    this.keySignature = keySignature;
    this.staffNumber = staffNumber;
  }

  public KeySignature getKeySignature() { return keySignature; }

  public int getStaffNumber() { return staffNumber; }

  public Fraction getOffset() { return offset; }

  public Staff getStaff() { return staff; }
  public void setStaff(Staff staff) { this.staff = staff; }
  public boolean isRest() { return false; }
}
