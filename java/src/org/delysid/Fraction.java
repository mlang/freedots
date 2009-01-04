/* -*- c-basic-offset: 2; -*- */
package org.delysid;

/**
 * Represents a fraction value.
 */
public class Fraction {
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
}
