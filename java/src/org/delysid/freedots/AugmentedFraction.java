/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots;

import org.delysid.freedots.model.AbstractPitch;

public class AugmentedFraction extends Fraction {
  int dots;

  public AugmentedFraction(int numerator, int denominator, int dots) {
    super(numerator, denominator);
    this.dots = dots;
    simplify();
  }
  public AugmentedFraction(Fraction duration) {
    this(duration.getNumerator(), duration.getDenominator(), 0);
    if (denominator == 2 || denominator == 4 || denominator == 8 ||
        denominator == 16 || denominator == 32 || denominator == 64 ||
        denominator == 128 || denominator == 256) {
      for (int dot = 10; dot > 0; dot--) {
        if (numerator == (int)(Math.pow(2, dot+1)-1)) {
          numerator -= 1;
          dots += 1;
          simplify();
        }
      }
    }
    if (denominator == 1) {
      if (numerator == 3) {
        numerator = 2;
        dots += 1;
      } else if (numerator == 6) {
        numerator = 4;
        dots += 1;
      } else if (numerator == 7) {
        numerator = 4;
        dots += 2;
      }
    }
  }
  public int getDots() { return dots; }

  public float toFloat() {
    final float undottedValue = numerator / denominator;
    float rest = undottedValue;
    for (int dot = 0; dot < dots; dot++) rest /= 2;
    return (undottedValue * 2) - rest;    
  }
  public String toBrailleString(AbstractPitch pitch) {
    String braille = "";
    int log = Util.log2(denominator);
    if (pitch != null) {
      int[] stepDots = { 145, 15, 124, 1245, 125, 24, 245 };
      int[] denomDots = { 36, 3, 6, 0 };
      braille += Braille.unicodeBraille(
                   Braille.dotsToBits(stepDots[pitch.getStep()])
                 | Braille.dotsToBits(denomDots[log > 3? log-4: log]));
    } else { /* Rest */
      int[] restDots = { 134, 136, 1236, 1346 };
      braille += Braille.unicodeBraille(Braille.dotsToBits(restDots[log > 3? log-4: log]));
    }

    for (int dot = 0; dot < dots; dot++) braille += Braille.dot;

    return braille;
  }
}
