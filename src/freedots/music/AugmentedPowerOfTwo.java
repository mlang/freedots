/* -*- c-basic-offset: 2; indent-tabs-mode: nil; -*- */
/*
 * FreeDots -- MusicXML to braille music transcription
 *
 * Copyright 2008-2010 Mario Lang  All Rights Reserved.
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

import java.util.ArrayList;
import java.util.List;

import freedots.math.Fraction;
import freedots.math.AbstractFraction;
import freedots.math.PowerOfTwo;

/** Represents a musical duration with augmentation dots and
 *  time modification.
 * <p>
 * This class is based on the idea that the duration of a note or rest is
 * always fundamentally a power of two.  Augmentation (or prolongation)
 * dots can be added to alter the initial value.
 * Additionally, if a note or rest is part of a tuplet group, its actual
 * value is further modified.
 */
public class AugmentedPowerOfTwo extends PowerOfTwo {
  private final int dots, normalNotes, actualNotes;

  /** Constructs a new power of two with the specified number of prolongation
   *  dots.
   * @param value is the base value of this augmented power of two
   * @param dots is the amount of prolongation dots
   */
  public AugmentedPowerOfTwo(final PowerOfTwo value, final int dots) {
    this(value, dots, 1, 1);
  }
  /** Constructs a new augmented power of two.
   * <p>
   * The final value is computed as follows:
   * (<i>value</i>&times;2&nbsp;&minus;&nbsp;<i>value</i>&divide;2<sup><i>dots</i></sup>)&nbsp;&times;&nbsp;<i>normalNotes</i>&nbsp;&divide;&nbsp;<i>actualNotes</i>
   * @param value is a power of two
   * @param dots indicates the number of augmentation dots
   */
  public AugmentedPowerOfTwo(final PowerOfTwo value, final int dots,
                             final int normalNotes, final int actualNotes) {
    super(value.getPower());
    this.dots = dots;
    this.normalNotes = normalNotes;
    this.actualNotes = actualNotes;
  }
  /** Calculates the actual numerator of this augmented power of two.
   */
  @Override public int numerator() {
    Fraction value = new Fraction(super.numerator(), super.denominator());
    return value.multiply(2).subtract(value.divide(pow2(dots)))
      .multiply(normalNotes).divide(actualNotes).numerator();
  }
  /** Calculates the actual denominator of this augmented power of two.
   */
  @Override public int denominator() {
    Fraction value = new Fraction(super.numerator(), super.denominator());
    return value.multiply(2).subtract(value.divide(pow2(dots)))
      .multiply(normalNotes).divide(actualNotes).denominator();
  }
  /** Returns the amount of augmentation dots attached to this power of two.
   * @return a positive integer
   */
  public int dots() { return dots; }
  /** Returns the numer of normal notes of a tuplet group.
   * @return 1 if no time modification applies to this value
   */
  public int normalNotes() { return normalNotes; }
  /** Returns the numer of actual notes in a tuplet group.
   * @return 1 if no time modification applies to this value
   * @see #normalNotes
   */
  public int actualNotes() { return actualNotes; }

  /** Returns a string representation of this value (including dots).
   */
  @Override public String toString() {
    StringBuilder sb = new StringBuilder(String.valueOf(super.numerator()));
    if (super.denominator() != 1) sb.append('/').append(super.denominator());
    for (int i = 0; i < dots; i++) sb.append('.');
    return sb.toString();
  }

  /* Musically relevant constants */
  public static final PowerOfTwo LONGA = new PowerOfTwo(2);
  public static final PowerOfTwo BREVE = new PowerOfTwo(1);
  public static final PowerOfTwo SEMIBREVE = new PowerOfTwo(0);
  public static final PowerOfTwo MINIM = new PowerOfTwo(-1);
  public static final PowerOfTwo CROTCHET = new PowerOfTwo(-2);
  public static final PowerOfTwo QUAVER = new PowerOfTwo(-3);
  public static final PowerOfTwo SEMIQUAVER = new PowerOfTwo(-4);
  public static final PowerOfTwo DEMISEMIQUAVER = new PowerOfTwo(-5);
  public static final PowerOfTwo HEMIDEMISEMIQUAVER = new PowerOfTwo(-6);
  public static final PowerOfTwo SEMIHEMIDEMISEMIQUAVER = new PowerOfTwo(-7);

  /** Tries to guess power and augmentation dots from a fractional value.
   * @throws IllegalArgumentException if the value could not be converted
   */
  // TODO: Handle time modification somehow, perhaps a big table?
  public static AugmentedPowerOfTwo valueOf(final AbstractFraction value) {
    if (value instanceof AugmentedPowerOfTwo) return (AugmentedPowerOfTwo)value;
    Fraction fraction = new Fraction(value).simplify();
    if (!fraction.isDyadic())
      throw new IllegalArgumentException(value.toString());
    List<AugmentedPowerOfTwo> parts = decompose(fraction, LONGA);
    if (parts.size() != 1)
      throw new IllegalArgumentException(value.toString() + parts);

    return parts.get(0);
  }
  /** Decompose an arbitrary fractional value into a list of augmented
   *  powers of two.
   */
   // This method is largely based on an idea from Samuel Thibault
  public static List<AugmentedPowerOfTwo> decompose(Fraction fraction,
                                                    PowerOfTwo largest) {
    List<AugmentedPowerOfTwo> list = new ArrayList<AugmentedPowerOfTwo>();
    Fraction f = fraction.divide(largest);
    while (f.numerator() > 0) {
      int x = f.numerator();
      int y = f.denominator();
      if (y == 1) {
        for (int i = 0; i < x; i++)
          list.add(new AugmentedPowerOfTwo(largest, 0));
        return list;
      }
      int n = firstZeroBit(x);
      int m = firstOneBit(y);
      /* x is always odd, so n is always at least 1 */
      AugmentedPowerOfTwo af =
        new AugmentedPowerOfTwo(new PowerOfTwo((n-1)-m).multiply(largest),
                                n-1);
      list.add(af);
      f = f.subtract(new Fraction((1<<n)-1, y));
    }
    return list;
  }
  private static int firstZeroBit(int i) {
    int bit = 0;
    while ((i & 1<<bit) == 1<<bit) bit++;
    return bit;
  }
  private static int firstOneBit(int i) {
    int bit = 0;
    while ((i & 1<<bit) != 1<<bit) bit++;
    return bit;
  }
}
