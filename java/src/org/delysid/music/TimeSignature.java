/* -*- c-basic-offset: 2; -*- */
package org.delysid.music;

import org.delysid.freedots.Fraction;

public class TimeSignature extends Fraction {
  public TimeSignature(int numerator, int denominator) {
    super(numerator, denominator);
  }
  public boolean equals(TimeSignature other) {
    return this.getNumerator()==other.getNumerator() &&
           this.getDenominator()==other.getDenominator();
  }
}
