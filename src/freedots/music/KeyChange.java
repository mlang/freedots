/* -*- c-basic-offset: 2; indent-tabs-mode: nil; -*- */
/*
 * FreeDots -- MusicXML to braille music transcription
 *
 * Copyright 2008-2010 Mario Lang  All Rights Reserved.
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
package freedots.music;

import freedots.math.Fraction;

public class KeyChange implements StaffElement {
  private Fraction offset;
  private KeySignature keySignature;
  private int staffNumber;
  private Staff staff;

  public KeyChange(final Fraction offset,
                   final KeySignature keySignature, final int staffNumber) {
    this.offset = offset;
    this.keySignature = keySignature;
    this.staffNumber = staffNumber;
  }

  public KeySignature getKeySignature() { return keySignature; }

  public int getStaffNumber() { return staffNumber; }

  public Fraction getOffset() { return offset; }
  public boolean equalsIgnoreOffset(Event other) {
    if (other instanceof KeyChange) {
      KeyChange otherKeyChange = (KeyChange)other;
      if (getStaffNumber() == otherKeyChange.getStaffNumber()
       && keySignature.equals(otherKeyChange.getKeySignature()))
        return true;
    }
    return false;
  }

  public Staff getStaff() { return staff; }
  public void setStaff(Staff staff) { this.staff = staff; }
  public boolean isRest() { return false; }
}
