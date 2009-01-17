/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.model;

import org.delysid.freedots.Braille;

public abstract class AbstractPitch {
  public abstract int getStep();
  public abstract int getAlter();
  public abstract int getOctave();
  public int getMIDIPitch() {
    int[] stepToChromatic = { 0, 2, 4, 5, 7, 9, 11 };
    int midiPitch = getOctave()*12 + stepToChromatic[getStep()] + getAlter();
    return midiPitch;
  }
  public Braille getOctaveSign(AbstractPitch lastPitch) {
    if (lastPitch != null) {
      int halfSteps = Math.abs(getMIDIPitch() - lastPitch.getMIDIPitch());

      if ((halfSteps < 5) ||
          (halfSteps >= 5 && halfSteps <= 7 &&
           getOctave() == lastPitch.getOctave()))
        return null;
    }
    return Braille.octave(getOctave());
  }
}
