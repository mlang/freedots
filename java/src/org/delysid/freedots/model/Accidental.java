/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.model;

import org.delysid.freedots.Braille;

public enum Accidental {
                       none(0),
                       NATURAL(0),
              FLAT(-1),           SHARP(1),
      QUARTER_FLAT(-0.5), QUARTER_SHARP(0.5);

  double alter;
  Accidental(double alter) { this.alter = alter; }
  public double getAlter() { return alter; }
  public Braille toBraille() {
    switch (this) {
      case NATURAL: return Braille.natural;
      case FLAT:    return Braille.flat;
      case SHARP:   return Braille.sharp;
    }
    return null;
  }
}
