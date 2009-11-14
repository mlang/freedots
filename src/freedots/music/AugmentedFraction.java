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
package freedots.music;

import freedots.Braille;

public class AugmentedFraction extends Fraction {
  private int dots;

  private int normalNotes, actualNotes;

  public AugmentedFraction (
    final int numerator, final int denominator, final int dots
  ) {
    this(numerator, denominator, dots, 1, 1);
  }
  /**
   * Copy constructor.
   */
  public AugmentedFraction(final AugmentedFraction augmentedFraction) {
    this(augmentedFraction.getNumerator(), augmentedFraction.getDenominator(),
         augmentedFraction.getDots(),
         augmentedFraction.normalNotes, augmentedFraction.actualNotes);
  }
  public AugmentedFraction (
    final int numerator, final int denominator, final int dots,
    final int normalNotes, final int actualNotes
  ) {
    super(numerator, denominator);
    simplify();
    this.dots = dots;
    this.normalNotes = normalNotes;
    this.actualNotes = actualNotes;    
  }
  public AugmentedFraction(Fraction duration) {
    this(duration.getNumerator(), duration.getDenominator(), 0);
    if (denominatorIsPowerOfTwo()) {
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
  public final int getDots() { return dots; }

  public final int getNormalNotes() { return normalNotes; }
  public final int getActualNotes() { return actualNotes; }

  @Override
  public float toFloat() {
    final float undottedValue = super.toFloat();
    float rest = undottedValue;
    for (int dot = 0; dot < dots; dot++) rest /= 2.;
    return (((undottedValue * 2) - rest) * (float)normalNotes) / (float)actualNotes;    
  }

  @Override
  public String toString() {
    String result = super.toString();
    for (int i = 0; i < dots; i++) result += ".";
    return result;
  }

  @Override
  public Fraction basicFraction() {
    Fraction undotted = new Fraction(numerator, denominator);
    Fraction rest = new Fraction(numerator, denominator);
    for (int i = 0; i < dots; i++) {
      rest = rest.divide(new Fraction(2, 1));
    }
    Fraction basicFraction = undotted.multiply(new Fraction(2, 1)).subtract(rest).multiply(new Fraction(normalNotes, 1)).divide(new Fraction(actualNotes, 1));
    return basicFraction;
  }

  public String toBrailleString(AbstractPitch pitch) {
    String braille = "";
    int log = getLog();
    if (pitch != null) {
      int[] stepDots = { 145, 15, 124, 1245, 125, 24, 245 };
      int[] denomDots = { 36, 3, 6, 0 };
      braille += Braille.unicodeBraille(
                   Braille.dotsToBits(stepDots[pitch.getStep()])
                 | Braille.dotsToBits(denomDots[log > 5? log-6: log-2]));
    } else { /* Rest */
      int[] restDots = { 134, 136, 1236, 1346 };
      braille += Braille.unicodeBraille(Braille.dotsToBits(restDots[log > 5? log-6: log-2]));
    }

    for (int dot = 0; dot < dots; dot++) braille += Braille.dot;

    return braille;
  }

  public static final int EIGHTH = 5;
  public static final int SIXTEENTH = 6;

  private boolean denominatorIsPowerOfTwo() {
    if (denominator == 2 || denominator == 4 || denominator == 8 ||
        denominator == 16 || denominator == 32 || denominator == 64 ||
        denominator == 128 || denominator == 256)
      return true;
    return false;
  }
}
