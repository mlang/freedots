package org.delysid.freedots.model;

import org.delysid.freedots.Braille;

public enum Ornament {
  mordent, turn;

  public Braille toBraille() {
    switch (this) {
      case mordent: return Braille.mordent;
      case turn: return Braille.turn;
    }

    return null;
  }
}
