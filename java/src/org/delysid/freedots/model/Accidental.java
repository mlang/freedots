/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.model;

public enum Accidental {
                       NATURAL(0),
              FLAT(-1),           SHARP(1),
      QUARTER_FLAT(-0.5), QUARTER_SHARP(0.5);

  double alter;
  Accidental(double alter) { this.alter = alter; }
  public double getAlter() { return alter; }
}

