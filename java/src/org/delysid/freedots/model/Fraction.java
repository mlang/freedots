/* -*- c-basic-offset: 2; -*- */
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
 * This software is maintained by Mario Lang <mlang@delysid.org>.
 */
package org.delysid.freedots.model;

/**
 * Represents a fraction value.
 */
public class Fraction implements Comparable<Fraction> {
  int numerator;
  int denominator;
  static final Fraction FOUR = new Fraction(4, 1);
    
  public Fraction(int numerator, int denominator) {
    this.numerator = numerator;
    this.denominator = denominator;
  }
    
  public int getNumerator() { return numerator; }

  public int getDenominator() { return denominator; }
  public void setDenominator(int denominator) { this.denominator = denominator; }

  public int getLog() {
    Fraction normalized = new Fraction(numerator, denominator).divide(FOUR);
    return (int)Math.round(Math.log(normalized.denominator) / Math.log(2));
  }
  public void setFromLog(int log) {
    Fraction
    wrapper = new Fraction(1, (int)Math.round(Math.pow(2, log))).multiply(FOUR);
    numerator = wrapper.numerator;
    denominator = wrapper.denominator;
  }

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
    Fraction newFraction = new Fraction(this.basicFraction().numerator * other.basicFraction().denominator +
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
