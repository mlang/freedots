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
package freedots.math;

/**
 * @see <a href="http://en.wikipedia.org/wiki/Fraction_(mathematics)">Wikipedia:
 *      Fraction (mathematics)</a>
 */
public abstract class AbstractFraction
  extends Number implements Comparable<Number> {
  public abstract int numerator();
  public abstract int denominator();

  public int intValue() { return (int)longValue(); }
  public float floatValue() { return (float)doubleValue(); }
  public long longValue() { return numerator() / denominator(); }
  public double doubleValue() { return (double)numerator() / denominator(); }

  public int compareTo(final Number number) {
    return Double.compare(doubleValue(), number.doubleValue());
  }

  @Override public boolean equals(final Object other) {
    if (this == other) return true;

    if (other instanceof AbstractFraction) {
      AbstractFraction that = (AbstractFraction)other;
      return this.numerator() == that.numerator()
          && this.denominator() == that.denominator();
    }

    if (other instanceof Number)
      return doubleValue() == ((Number)other).doubleValue();

    return false;
  }

  /** Returns {@code true} if {@link #denominator} is a power of two.
   * @see <a href="http://en.wikipedia.org/wiki/Dyadic_fraction">Wikipedia:
   *      Dyadic fraction</a>
   */
  public boolean isDyadic() { return Integer.bitCount(denominator()) == 1; }

  /** Computes the largest positive integer that divides
   *  {@link #numerator} and {@link #denominator} without a remainder.
   * @see <a href="http://en.wikipedia.org/wiki/Greatest_common_divisor">
   *      Wikipedia: Greatest common divisor</a>
   */
  public int greatestCommonDivisor() { return gcd(numerator(), denominator()); }

  /** Returns the multiplicative inverse of a fraction.
   * @see <a href="http://en.wikipedia.org/wiki/Multiplicative_inverse">
   *      Wikipedia: Multiplicative inverse</a>
   */
  public Fraction reciprocal() {
    return new Fraction(denominator(), numerator());
  }
  /** Inverts the sign of a fraction.
   */
  public Fraction negate() {
    return new Fraction(-numerator(), denominator());
  }

  public Fraction add(final AbstractFraction other) {
    final int an = this.numerator();
    final int ad = this.denominator();
    final int bn = other.numerator();
    final int bd = other.denominator();
    return new Fraction(an*bd + bn*ad, ad*bd).simplify();
  }      
  public Fraction subtract(final AbstractFraction other) {
    return add(other.negate());
  }
  public Fraction multiply(final AbstractFraction other) {
    return new Fraction(this.numerator() * other.numerator(),
                        this.denominator() * other.denominator()).simplify();
  }
  public Fraction multiply(final Integer other) {
    return new Fraction(this.numerator()*other, this.denominator()).simplify();
  }
  public Fraction divide(final AbstractFraction other) {
    return new Fraction(this.numerator() * other.denominator(),
                        this.denominator() * other.numerator()).simplify();
  }
  public Fraction divide(final Integer other) {
    return new Fraction(numerator(), denominator()*other).simplify();
  }

  @Override public String toString() {
    if (denominator() == 1) return String.valueOf(numerator());
    return String.valueOf(numerator()) + "/" + denominator();
  }

  private static int gcd(final int a, final int b) {
    return b == 0? a: gcd(b, a%b);
  }
}

