/* -*- c-basic-offset: 2; -*- */
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
