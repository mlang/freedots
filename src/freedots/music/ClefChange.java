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

import freedots.Braille;
import freedots.math.Fraction;

/** Indicates a change of a clef at a certain musical duration and staff.
 */
public final class ClefChange implements StaffElement {
  private Fraction offset;
  private Clef clef;
  private int staffNumber;
  private Staff staff;

  public ClefChange(final Fraction offset,
                    final Clef clef, final int staffNumber) {
    this.offset = offset;
    this.clef = clef;
    this.staffNumber = staffNumber;
  }

  public Clef getClef() { return clef; }

  public int getStaffNumber() { return staffNumber; }

  public Fraction getOffset() { return offset; }

  public Staff getStaff() { return staff; }
  public void setStaff(Staff staff) { this.staff = staff; }
  public boolean isRest() { return false; }

  @Deprecated
  public String toBrailleString() {
    if (offset.compareTo(Fraction.ZERO) > 0) {
      Clef initialClef = staff.getClef();
      if (initialClef.isBass())
        if (clef.isTreble()) {
          return String.valueOf(Braille.unicodeBraille(Braille.dotsToBits(345)))
                 + Braille.unicodeBraille(Braille.dotsToBits(34))
                 + Braille.unicodeBraille(Braille.dotsToBits(13));
        }
      else if (initialClef.isTreble())
        if (clef.isBass()) {
          return "" + Braille.unicodeBraille(Braille.dotsToBits(345)) +
                 Braille.unicodeBraille(Braille.dotsToBits(3456)) +
                 Braille.unicodeBraille(Braille.dotsToBits(13));
        }
    }
    if (clef.isTreble())
      return "" + Braille.unicodeBraille(Braille.dotsToBits(345)) +
             Braille.unicodeBraille(Braille.dotsToBits(34)) +
             Braille.unicodeBraille(Braille.dotsToBits(123));
    else if (clef.isBass())
      return "" + Braille.unicodeBraille(Braille.dotsToBits(345)) +
             Braille.unicodeBraille(Braille.dotsToBits(3456)) +
             Braille.unicodeBraille(Braille.dotsToBits(123));
    return "UNHANDLED CLEF";
  }
  public boolean equalsIgnoreOffset(Event other) {
    if (other instanceof ClefChange) {
      return clef.equals(((ClefChange)other).getClef());
    }
    return false;
  }
}
