/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.model;

public abstract class AbstractPitch {
  public abstract int getStep();
  public abstract int getAlter();
  public abstract int getOctave();
  public int getMIDIPitch() {
    int[] stepToChromatic = { 0, 2, 4, 5, 7, 9, 11 };
    int midiPitch = getOctave()*12 + stepToChromatic[getStep()] + getAlter();
    return midiPitch;
  }
}
