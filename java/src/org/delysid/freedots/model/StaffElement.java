/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.model;

public interface StaffElement extends Event {
  public int getStaffNumber();
  public Staff getStaff();
  public void setStaff(Staff staff);
  public boolean isRest();
}
