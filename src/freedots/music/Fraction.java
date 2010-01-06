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
import java.util.Collections;
import java.util.List;

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

  public final int getNumerator() { return numerator; }
  public final int getDenominator() { return denominator; }

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
    return denominator == 1? Integer.toString(numerator)
           : Integer.toString(numerator) + "/" + Integer.toString(denominator);
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

  /** Converts an arbitrary fractional value to a list of fractions with
   *  augmentation dots.
   * @return a {@link java.util.List} of
   *         {@link freedots.music.AugmentedFraction} instances
   */
  public List<AugmentedFraction> decompose() {
    List<AugmentedFraction> list = new ArrayList<AugmentedFraction>();
    Fraction f = new Fraction(getNumerator(), getDenominator());
    f.simplify();
    while (f.getNumerator() > 0) {
      f.simplify();
      int x = f.getNumerator();
      int y = f.getDenominator();
      if (y == 1) {
        for (int i = 0; i < x; i++) list.add(new AugmentedFraction(1, 1, 0));
        return list;
      }
      int n = firstZeroBit(x);
      /* x is always odd, so n is always at least 1 */
      AugmentedFraction af = new AugmentedFraction(1<<(n-1), y, n-1);
      list.add(af);
      f = f.subtract(new Fraction((1<<n)-1,y));
    }
    Collections.reverse(list);
    return list;
  }
  private static int firstZeroBit(int i) {
    int bit = 0;
    while ((i & 1<<bit) == 1<<bit) bit++;
    return bit;
  }
}
