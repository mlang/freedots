/* -*- c-basic-offset: 2; indent-tabs-mode: nil; -*- */
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
package org.delysid.freedots.transcription;

import org.delysid.freedots.Braille;

class BrailleString {
  private Object model = null;
  private Braille braille = null;
  private String string = null;
  private BrailleList container = null;
  BrailleString(Braille braille) {
    this.braille = braille;
  }
  BrailleString(String string) {
    this.string = string;
  }
  BrailleString(final String string, final Object model) {
    this(string);
    this.model = model;
  }
  Object getModel() { return model; }
  void setContainer(BrailleList list) { container = list; }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(string != null ? string : braille.toString());
    if (braille != null && braille.needsAdditionalDot3IfOneOfDot123Follows()) {
      if (container != null) {
        BrailleString next = container.getNext(this);
        if (next != null) {
          if (next.startsWithOneOf123()) {
            sb.append(Braille.dot.toString());
          }
        }
      }
    }
    return sb.toString();
  }
  public int length() { return toString().length(); }
  private boolean startsWithOneOf123() {
    String data = toString();
    if (data.length() > 0) {
      char firstChar = data.charAt(0);
      if (firstChar >= 0X2800 && firstChar <= 0X28FF) {
	if ((((int)firstChar) & 0X2807) > 0X2800) {
	  return true;
	}
      }
    }
    return false;
  }
}
