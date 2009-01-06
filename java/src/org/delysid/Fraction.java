/* -*- c-basic-offset: 2; -*- */
package org.delysid;

/**
 * Represents a fraction value.
 */
public class Fraction implements Comparable<Fraction> {
  int numerator;
  int denominator;
    
  /**
   * Creates a new {@code Fraction}.
   * 
   * @param numerator the numerator value.
   * @param denominator the denominator value.
   */
  public Fraction(int numerator, int denominator) {
    this.numerator = numerator;
    this.denominator = denominator;
  }
    
  /**
   * Gets the numerator of the {@code Fraction}
   * 
   * @return the numerator as an integer.
   */
  public int getNumerator() {
    return numerator;
  }
    
  /**
   * Gets the denominator of the {@code Fraction}
   * 
   * @return the denominator as an integer.
   */
  public int getDenominator() {
    return denominator;
  }

  /**
   * Gets the float value of the {@code Fraction}
   * 
   * @return the value as a float.
   */
  public float toFloat() {
    return numerator / denominator;
  }

  public int toInteger(int divisions) {
    return this.divide(new Fraction(1, 4*divisions)).numerator;
  }

  /**
   * Compares this {@code Fraction} with the specified object for order.
   * 
   * @return a negative integer, zero, or a positive integer.
   */
  public int compareTo(Fraction other) {
    return Float.compare(this.toFloat(), other.toFloat());
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
