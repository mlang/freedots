/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.model;

public interface StaffElement extends Event {
  public String getStaffName();
  public void setStaff(Staff staff);
  public boolean isRest();
}
