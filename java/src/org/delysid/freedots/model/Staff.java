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

public class Staff extends MusicList {
  public Timeline<KeySignature>
  keyList = new Timeline<KeySignature>(new KeySignature(0));
  public Timeline<Clef>
  clefList = new Timeline<Clef>(new Clef(Clef.Sign.G, 2));
  public Clef getClef() { return clefList.get(new Fraction(0, 1)); }
  public Clef getClef(Fraction offset) { return clefList.get(offset); }
  public KeySignature getKeySignature(Fraction offset) {
    return keyList.get(offset);
  }

  public Staff() { super(); }

  public boolean add(Event event) {
    if (super.add(event)) {
      if (event instanceof StaffElement) ((StaffElement)event).setStaff(this);
      if (event instanceof ClefChange) {
        ClefChange clefChange = (ClefChange)event;
        clefList.put(clefChange.getOffset(), clefChange.getClef());
      } else if (event instanceof KeyChange) {
        KeyChange keyChange = (KeyChange)event;
        keyList.put(keyChange.getOffset(), keyChange.getKeySignature());
      }
      return true;
    }
    return false;
  }
  public boolean containsChords() {
    for (Event event:this) if (event instanceof StaffChord) return true;
    return false;
  }
}
