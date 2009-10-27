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
package org.delysid.freedots.model;

import org.delysid.freedots.Braille;

public enum Accidental {
                       NATURAL(0),
              FLAT(-1),           SHARP(1),
       DOUBLE_FLAT(-2),    DOUBLE_SHARP(2),
      QUARTER_FLAT(-0.5), QUARTER_SHARP(0.5);

  private double alter;
  Accidental(final double alter) { this.alter = alter; }
  public double getAlter() { return alter; }
  public Braille toBraille() {
    switch (this) {
      case NATURAL: return Braille.natural;
      case DOUBLE_FLAT: return Braille.doubleFlat;
      case FLAT:    return Braille.flat;
      case SHARP:   return Braille.sharp;
      case DOUBLE_SHARP: return Braille.doubleSharp;
    }
    return null;
  }

  public static Accidental fromAlter(double alter) {
    for (Accidental accidental: Accidental.values())
      if (accidental.getAlter() == alter) return accidental;

    return null;
  }
}
