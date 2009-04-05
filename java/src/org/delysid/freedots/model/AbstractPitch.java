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

public abstract class AbstractPitch implements Comparable<AbstractPitch> {
  public abstract int getStep();
  public abstract int getAlter();
  public abstract int getOctave();
  public int getMIDIPitch() {
    int[] stepToChromatic = { 0, 2, 4, 5, 7, 9, 11 };
    int midiPitch = (getOctave()+1)*12 + stepToChromatic[getStep()] + getAlter();
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
