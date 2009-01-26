/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.model;

import org.delysid.freedots.Braille;

public abstract class AbstractPitch implements Comparable<AbstractPitch> {
  public abstract int getStep();
  public abstract int getAlter();
  public abstract int getOctave();
  public int getMIDIPitch() {
    int[] stepToChromatic = { 0, 2, 4, 5, 7, 9, 11 };
    int midiPitch = getOctave()*12 + stepToChromatic[getStep()] + getAlter();
    return midiPitch;
  }
  @Override
  public boolean equals(Object object) {
    if (object instanceof AbstractPitch) {
      AbstractPitch other = (AbstractPitch)object;
      if (this.getStep() == other.getStep() &&
          this.getAlter() == other.getAlter() &&
          this.getOctave() == other.getOctave()) return true;
    }
    return false;
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
  public int diatonicDifference(AbstractPitch other) {
    return ((this.getOctave()*7) + this.getStep()) -
           ((other.getOctave()*7) + other.getStep());
  }
  public int compareTo(AbstractPitch other) {
    int diatonicDifference = diatonicDifference(other);
    if (diatonicDifference != 0) return diatonicDifference;
    else return getAlter() < other.getAlter()? -1: getAlter()==other.getAlter()? 0: 1;
  }
}
