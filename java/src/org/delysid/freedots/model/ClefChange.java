/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.model;

import org.delysid.freedots.Braille;

public class ClefChange implements StaffElement {
  private Fraction offset;
  private Clef clef;
  private String staffName = null;
  private Staff staff;

  public ClefChange(Fraction offset, Clef clef, String staffName) {
    this.offset = offset;
    this.clef = clef;
    this.staffName = staffName;
  }

  public Clef getClef() { return clef; }

  public String getStaffName() { return staffName; }

  public Fraction getOffset() { return offset; }

  public Staff getStaff() { return staff; }
  public void setStaff(Staff staff) { this.staff = staff; }
  public boolean isRest() { return false; }

  public String toBrailleString() {
    if (offset.compareTo(new Fraction(0, 1)) > 0) {
      Clef initialClef = staff.getClef();
      if (initialClef.isBass())
        if (clef.isTreble()) {
          return "" + Braille.unicodeBraille(Braille.dotsToBits(345)) +
                 Braille.unicodeBraille(Braille.dotsToBits(34)) +
                 Braille.unicodeBraille(Braille.dotsToBits(13));
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
}
