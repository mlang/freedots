/* -*- c-basic-offset: 2; -*- */
/*
 * FreeDots -- MusicXML to braille music transcription
 *
 * Copyright 2008-2009 Mario Lang  All Rights Reserved.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 3, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details (a copy is included in the LICENSE.txt file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License 
 * along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This file is maintained by Mario Lang <mlang@delysid.org>.
 */
package org.delysid.freedots.model;

public class StartBar extends VerticalEvent {
  int measureNumber;
  public StartBar(Fraction offset, int measureNumber) {
    super(offset);
    this.measureNumber = measureNumber;
  }

  int staffCount;
  public int getStaffCount() { return staffCount; }
  public void setStaffCount(int staffCount) { this.staffCount = staffCount; }

  boolean newSystem = false;
  public boolean getNewSystem() { return newSystem; }
  public void setNewSystem(boolean newSystem) { this.newSystem = newSystem; }

  int endingStart = 0;
  public int getEndingStart() { return endingStart; }
  public void setEndingStart(int endingStart) { this.endingStart = endingStart; }

  boolean repeatForward = false;
  public boolean getRepeatForward() { return repeatForward; }
  public void setRepeatForward(boolean flag) { repeatForward = flag; }

  private TimeSignature timeSignature = null;
  public TimeSignature getTimeSignature() { return timeSignature; }
  public void setTimeSignature(TimeSignature timeSignature) {
    this.timeSignature = timeSignature;
  }
  public boolean equalsIgnoreOffset(Event other) {
    if (other instanceof StartBar) {
      StartBar otherBar = (StartBar)other;
      if (getStaffCount() == otherBar.getStaffCount() &&
	  getNewSystem() == otherBar.getNewSystem() &&
	  getEndingStart() == otherBar.getEndingStart()
	  )
	return true;
    }
    return false;
  }
}
