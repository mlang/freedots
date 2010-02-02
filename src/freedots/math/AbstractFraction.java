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

  @Override public boolean equals(Object other) {
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

  public Fraction add(AbstractFraction other) {
    final int an = this.numerator();
    final int ad = this.denominator();
    final int bn = other.numerator();
    final int bd = other.denominator();
    return new Fraction(an*bd + bn*ad, ad*bd).simplify();
  }      
  public Fraction negate() {
    return new Fraction(-numerator(), denominator());
  }
  public Fraction subtract(AbstractFraction other) {
    return add(other.negate());
  }
  public Fraction multiply(final AbstractFraction other) {
    return new Fraction(this.numerator() * other.numerator(),
                        this.denominator() * other.denominator()).simplify();
  }
  public Fraction multiply(final Integer other) {
    return new Fraction(this.numerator() * other, this.denominator());
  }
  public Fraction divide(final AbstractFraction other) {
    return new Fraction(this.numerator() * other.denominator(),
                        this.denominator() * other.numerator()).simplify();
  }
  public Fraction divide(final Integer other) {
    return new Fraction(numerator(), denominator() * other).simplify();
  }
  @Override public String toString() {
    if (denominator() == 1) return String.valueOf(numerator());
    return String.valueOf(numerator()) + "/" + denominator();
  }

  public int toInteger(final int ticksPerQuarter) {
    return divide(new Fraction(1, 4 * ticksPerQuarter)).numerator();
  }

  protected static int gcd(final int numerator, final int denominator) {
    return (numerator > denominator)
         ? calcGcd(numerator, denominator)
         : calcGcd(denominator, numerator);
  }
  private static int calcGcd(int larger, int smaller) {
    return smaller == 0? larger: calcGcd(smaller, larger%smaller);
  }
}
