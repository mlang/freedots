/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.model;

public class
StaffChord extends AbstractChord<StaffElement> implements StaffElement {
  private String staffName;

  public StaffChord(StaffElement initialNote) {
    super(initialNote);
    this.staffName = initialNote.getStaffName();
  }

  private Staff staff = null;

  public void setStaff(Staff staff) { this.staff = staff; }
  public String getStaffName() { return staffName; }

  /* FIXME: Seems inappropriate to have to impelement this */
  public boolean isRest() { return false; }
}

