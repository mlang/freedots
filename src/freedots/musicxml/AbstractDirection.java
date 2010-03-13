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
package freedots.musicxml;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import freedots.math.Fraction;
import freedots.music.Event;
import freedots.music.Staff;
import freedots.music.StaffElement;

public abstract class AbstractDirection implements StaffElement {
  protected final Element element;
  private final Fraction initialDate;
  private final int durationMultiplier, divisions;

  private Element offset = null, staffNumber = null;

  AbstractDirection(final Element element,
                    final int durationMultiplier, final int divisions,
                    final Fraction offset) {
    this.element = element;
    this.durationMultiplier = durationMultiplier;
    this.divisions = divisions;
    this.initialDate = offset;

    for (Node node = element.getFirstChild(); node != null;
         node = node.getNextSibling()) {
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        Element child = (Element)node;
        if ("offset".equals(child.getTagName())) {
          this.offset = child;
        } else if ("staff".equals(child.getTagName())) {
          staffNumber = child;
        }
      }
    }
  }

  /** Calculates the musical position of this object.
   * Harmony elements do have an optional offset element to modify the
   * initial musical position further.
   * This method takes that into account.
   * @return the musical position as a fractional value.
   */
  public Fraction getMoment() {
    if (offset != null) {
      int value = Integer.parseInt(offset.getTextContent());
      return initialDate.add(new Fraction(value * durationMultiplier,
                                          4 * divisions));
    }
    return initialDate;
  }
  public boolean equalsIgnoreOffset(Event object) {
    return false;
  }
  public int getStaffNumber() {
    if (staffNumber != null)
      return Integer.parseInt(staffNumber.getTextContent()) - 1;
    return 0;
  }
  private Staff staff = null;
  public Staff getStaff() { return staff; }
  public void setStaff(Staff staff) { this.staff = staff; }
  public boolean isRest() { return false; }
}
