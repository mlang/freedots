/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.model;

public class Clef {
  public enum Sign { G, F, C; };

  Sign sign;
  int line;
  public Clef(Sign sign, int line) {
    this.sign = sign;
    this.line = line;
  }
  public int getChordDirection() {
    switch (sign) {
      case G: switch (line) {
                case 2: return -1;
              }
      case F: switch (line) {
                case 4: return 1;
              }
    }
    return -1;
  }
}
