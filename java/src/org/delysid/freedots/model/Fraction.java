/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.model;

/**
 * Represents a fraction value.
 */
public class Fraction implements Comparable<Fraction> {
  int numerator;
  int denominator;
    
  public Fraction(int numerator, int denominator) {
    this.numerator = numerator;
    this.denominator = denominator;
  }
    
  public int getNumerator() { return numerator; }
  public int getDenominator() { return denominator; }

  public float toFloat() { return numerator / denominator; }
  public int toInteger(int divisions) {
    return this.divide(new Fraction(1, 4*divisions)).numerator;
  }

  public int compareTo(Fraction other) {
    return Float.compare(this.toFloat(), other.toFloat());
  }

  @Override
  public String toString() {
    return denominator == 1? Integer.toString(numerator):
                             Integer.toString(numerator) + "/" + Integer.toString(denominator);
  }
  public Fraction add(Fraction other) {
    Fraction newFraction = new Fraction(this.numerator * other.denominator +
					other.numerator * this.denominator,
					this.denominator * other.denominator);
    newFraction.simplify();
    return newFraction;
  }
  public Fraction subtract(Fraction other) {
    Fraction newFraction = new Fraction(this.numerator * other.denominator +
					-other.numerator * this.denominator,
					this.denominator * other.denominator);
    newFraction.simplify();
    return newFraction;
  }

  public Fraction divide(Fraction other) {
    Fraction newFraction = new Fraction(this.numerator * other.denominator,
					this.denominator * other.numerator);
    newFraction.simplify();
    return newFraction;
  }

  public void simplify() {
    int gcd = calcGcd(Math.max(numerator, denominator),
		      Math.min(numerator, denominator));
    numerator /= gcd;
    denominator /= gcd;
  }
  static int calcGcd(int larger, int smaller) {
    return smaller == 0? larger: calcGcd(smaller, larger%smaller);
  }
}
