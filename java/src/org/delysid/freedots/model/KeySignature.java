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

import org.delysid.freedots.Braille;

public class KeySignature {
  private final int type; // number of sharps (+) or flats (-)
  private final int[] modifiers;

  public KeySignature(int type)	{
    this.type = type;

    modifiers = new int[7];
    for (int i = 0; i < 7; i++) modifiers[i] = 0;
    if (type > 0) {
      for (int i=0; i<type; i++) modifiers[sharps[i]] = 1;
    } else if (type < 0) {
      for (int i=0; i<-type; i++) modifiers[flats[i]] = -1;
    }
  }

  public int getType() { return type; }

  public int getModifierCount() { return Math.abs(type); }

  public int getModifier(int reducedRank) {
    return modifiers[reducedRank];
  }

  private static final int[] sharps = {3, 0, 4, 1, 5, 2, 6};
  private static final int[] flats  = {6, 2, 5, 1, 4, 0, 3};

  public String toBraille() {
    if (type == 0) return "";
    else if (type < 0) return Braille.nTimes(Braille.flat, Math.abs(type));
    else return Braille.nTimes(Braille.sharp, type);
  }
}
