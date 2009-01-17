/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.model;

import org.delysid.freedots.Braille;

public class TimeSignature extends Fraction {
  public TimeSignature(int numerator, int denominator) {
    super(numerator, denominator);
  }
  public boolean equals(TimeSignature other) {
    return this.getNumerator()==other.getNumerator() &&
           this.getDenominator()==other.getDenominator();
  }
  public String toBraille() {
    String result = "";
    int number = denominator;
    while (number > 0) {
      int digit = number % 10;
      number /= 10;
      result = Braille.lowerDigit(digit) + result;
    }
    number = numerator;
    while (number > 0) {
      int digit = number % 10;
      number /= 10;
      result = Braille.upperDigit(digit) + result;
    }
    return Braille.numberSign + result;
  }
}
