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
 * Represents a fractional value.
 * <p>
 * If Java were a decent programming language (like Common Lisp) we wouldn't
 * need to reinvent the wheel yet again.
 */
public class Fraction implements Comparable<Fraction> {
  public static final Fraction ZERO = new Fraction(0, 1);
  public static final Fraction FOUR = new Fraction(4, 1);

  protected int numerator;
  protected int denominator;
 
  public Fraction(final int numerator, final int denominator) {
    this.numerator = numerator;
    this.denominator = denominator;
    // We do not simplify since subclasses might want to preserve the original
    // values of numerator and denominator
  }

  public int getNumerator() { return numerator; }
  public int getDenominator() { return denominator; }

  public float toFloat() { return (float)numerator / denominator; }

  public int toInteger(int divisions) {
    return this.divide(new Fraction(1, 4*divisions)).numerator;
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof Fraction) {
      Fraction other = (Fraction)object;
      return this.toFloat() == other.toFloat();
    }
    return false;
  }
  public int compareTo(Fraction other) {
    return Float.compare(this.toFloat(), other.toFloat());
  }
  public Fraction basicFraction() { return this; }

  @Override
  public String toString() {
    return denominator == 1? Integer.toString(numerator):
                             Integer.toString(numerator) + "/" + Integer.toString(denominator);
  }

  public Fraction negate() {
    Fraction basic = basicFraction();
    return new Fraction(-basic.numerator, basic.denominator);
  }
  public Fraction add(Fraction other) {
    Fraction newFraction = new Fraction(this.basicFraction().numerator * other.basicFraction().denominator +
                                        other.basicFraction().numerator * this.basicFraction().denominator,
                                        this.basicFraction().denominator * other.basicFraction().denominator);
    newFraction.simplify();
    return newFraction;
  }
  public Fraction subtract(Fraction other) {
    Fraction
      newFraction = new Fraction(this.basicFraction().numerator * other.basicFraction().denominator +
                                 -other.basicFraction().numerator * this.basicFraction().denominator,
                                 this.basicFraction().denominator * other.basicFraction().denominator);
    newFraction.simplify();
    return newFraction;
  }

  public Fraction divide(Fraction other) {
    Fraction newFraction = new Fraction(this.basicFraction().numerator * other.basicFraction().denominator,
                                        this.basicFraction().denominator * other.basicFraction().numerator);
    newFraction.simplify();
    return newFraction;
  }

  public Fraction multiply(Fraction other) {
    Fraction newFraction = new Fraction(this.basicFraction().numerator * other.basicFraction().numerator,
                                        this.basicFraction().denominator * other.basicFraction().denominator);
    newFraction.simplify();
    return newFraction;
  }

  void simplify() {
    int gcd = calcGcd(Math.max(numerator, denominator),
                      Math.min(numerator, denominator));
    numerator /= gcd;
    denominator /= gcd;
  }
  static int calcGcd(int larger, int smaller) {
    return smaller == 0? larger: calcGcd(smaller, larger%smaller);
  }
}
