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

/**
 * An {@code AugmentedFraction} encapsulates the concept of musical
 * duration with augmentation dots and time modifications.
 */
public class AugmentedFraction extends Fraction {
  private int dots;
  private int normalNotes, actualNotes;

  public AugmentedFraction(
    final int numerator, final int denominator, final int dots
  ) {
    this(numerator, denominator, dots, 1, 1);
  }
  /** Copy constructor.
   * @param augmentedFraction is the object to take initial values from.
   */
  public AugmentedFraction(final AugmentedFraction augmentedFraction) {
    this(augmentedFraction.getNumerator(), augmentedFraction.getDenominator(),
         augmentedFraction.getDots(),
         augmentedFraction.normalNotes, augmentedFraction.actualNotes);
  }
  public AugmentedFraction(
    final int numerator, final int denominator, final int dots,
    final int normalNotes, final int actualNotes
  ) {
    super(numerator, denominator);
    simplify();
    this.dots = dots;
    this.normalNotes = normalNotes;
    this.actualNotes = actualNotes;    
  }
  /**
   * Construct an {@code AugmentedFraction} from a basic {@link Fraction}.
   *
   * This involved the calculation of augmentation dots and the real
   * fractional part.
   *
   * This is required for MusicXML note elements which do not specify
   * type and/or dots, but do specify the duration of the note.
   *
   * @param duration is the fractional value to deduce dots from.
   */
  public AugmentedFraction(final Fraction duration) {
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
  /** Describes how many notes are played in the time usually occupied by the
   *  number of normalNotes.
   */
  public final int getActualNotes() { return actualNotes; }

  public final int getLog() {
    Fraction normalized = new Fraction(numerator, denominator).divide(FOUR);
    return (int)Math.round(Math.log(normalized.denominator) / Math.log(2));
  }
  public final void setFromLog(int log) {
    Fraction
    wrapper = new Fraction(1, (int)Math.round(Math.pow(2, log))).multiply(FOUR);
    numerator = wrapper.numerator;
    denominator = wrapper.denominator;
  }

  @Override
  public float toFloat() {
    final float undottedValue = super.toFloat();
    float rest = undottedValue;
    for (int dot = 0; dot < dots; dot++) rest /= 2.;
    return (undottedValue * 2 - rest)
      * (float)normalNotes / (float)actualNotes;    
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

    return undotted.multiply(new Fraction(2, 1)).subtract(rest)
      .multiply(new Fraction(normalNotes, 1))
      .divide(new Fraction(actualNotes, 1));
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
