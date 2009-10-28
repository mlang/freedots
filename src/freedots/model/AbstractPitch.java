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
package freedots.model;

import freedots.Braille;

/**
 * An abstract representation of pitch with step, alter and octave values.
 */
public abstract class AbstractPitch implements Comparable<AbstractPitch> {
  /**
   * @return the step (a value from 0 to 6)
   */
  public abstract int getStep();
  /**
   * @return alteration (usually between -2 and 2)
   */
  public abstract int getAlter();
  /**
   * @return the octave
   */
  public abstract int getOctave();

  public final int getMIDIPitch() {
    int midiPitch = (getOctave()+1) * CHROMATIC_STEPS
                  + STEP_TO_CHROMATIC[getStep()]
                  + getAlter();
    return midiPitch;
  }
  @Override
  public boolean equals(Object object) {
    if (object instanceof AbstractPitch) {
      AbstractPitch other = (AbstractPitch)object;
      if (this.getStep() == other.getStep()) {
        if (this.getAlter() == other.getAlter()) {
          if (this.getOctave() == other.getOctave()) return true;
        }
      }
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
    return ((this.getOctave()*STEPS) + this.getStep()) -
           ((other.getOctave()*STEPS) + other.getStep());
  }
  public int compareTo(AbstractPitch other) {
    int diatonicDifference = diatonicDifference(other);
    if (diatonicDifference != 0) return diatonicDifference;
    else {
      final boolean flatter = getAlter() < other.getAlter();
      final boolean sharper = getAlter() > other.getAlter();
      return flatter ? -1 : sharper ? 1 : 0;
    }      
  }

  public AbstractPitch nextStep(AccidentalContext accidentalContext) {
    int octave = getOctave();
    int step = getStep();
    double alter = 0;
    if (step < STEPS-1) { step += 1; } else { octave += 1; step = 0; };
    alter = accidentalContext.getAlter(octave, step);
    return new TemporaryPitch(octave, step, (int)alter);
  }

  public AbstractPitch previousStep(AccidentalContext accidentalContext) {
    int octave = getOctave();
    int step = getStep();
    double alter = 0;
    if (step > 0) { step -= 1; } else { octave -= 1; step = STEPS-1; };
    alter = accidentalContext.getAlter(octave, step);
    return new TemporaryPitch(octave, step, (int)alter);
  }

  class TemporaryPitch extends AbstractPitch {
    private int octave, step, alter;
    TemporaryPitch(int octave, int step, int alter) {
      this.octave = octave;
      this.step = step;
      this.alter = alter;
    }
    public int getOctave() { return octave; }
    public int getStep() { return step; }
    public int getAlter() { return alter; }
  }

  @Override
  public String toString() {
    final String[] stepNames = new String[] {"C", "D", "E", "F", "G", "A", "B"};
    return stepNames[getStep()]+getOctave()+" ("+getAlter()+")";
  }
  private final static int STEPS = 7;
  private final static int CHROMATIC_STEPS = 12;
  private final static int[] STEP_TO_CHROMATIC = new int[] {
    0, 2, 4, 5, 7, 9, 11
  };
}
