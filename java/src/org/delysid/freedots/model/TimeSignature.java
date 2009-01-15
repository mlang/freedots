/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.model;

import org.delysid.freedots.Braille;
import org.delysid.freedots.Fraction;

public class TimeSignature extends Fraction {
  public TimeSignature(int numerator, int denominator) {
    super(numerator, denominator);
  }
  public boolean equals(TimeSignature other) {
    return this.getNumerator()==other.getNumerator() &&
           this.getDenominator()==other.getDenominator();
  }
  public String toBraille() {
    int[] upperDots = { 245, 1, 12, 14, 145, 15, 124, 1245, 125, 24 };
    int[] lowerDots = { 356, 2, 23, 25, 256, 26, 235, 2356, 236, 35 };
    String result = "";
    int number = denominator;
    while (number > 0) {
      int digit = number % 10;
      number /= 10;
      result = Braille.unicodeBraille(Braille.dotsToBits(lowerDots[number]))+result;
    }
    number = numerator;
    while (number > 0) {
      int digit = number % 10;
      number /= 10;
      result = Braille.unicodeBraille(Braille.dotsToBits(upperDots[number]))+result;
    }
    return Braille.numberSign + result;
  }
}
