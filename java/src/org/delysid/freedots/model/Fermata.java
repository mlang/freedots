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
 * This software is maintained by Mario Lang <mlang@delysid.org>.
 */
package org.delysid.freedots.model;

import org.delysid.freedots.Braille;

/**
 * A fermata indicates that the note should be sustained for longer than its
 * note value would indicate.  Exactly how much longer it is held is up to the
 * discretion of the performer, but twice as long is not unusual.  It is
 * usually printed above, but occasionally below (upside down), the note
 * that is to be held longer.  Occasionally holds are also printed above
 * rests or barlines, indicating a pause of indefinite duration.
 */
public class Fermata {
  private Type type = Type.UPRIGHT;
  private Shape shape = Shape.NORMAL;

  public Fermata(Type type, Shape shape) {
    this.type = type;
    this.shape = shape;
  }
  public Braille toBraille() {
    switch (shape) {
      case ANGLED: return Braille.fermataTent;
      case SQUARE: return Braille.fermataSquare;
    }
    return Braille.fermata;
  }

  public enum Shape { NORMAL, ANGLED, SQUARE; };
  public enum Type { UPRIGHT, INVERTED; }
}
