/* -*- c-basic-offset: 2; -*- */
import org.delysid.freedots.model.AugmentedFraction;
import org.delysid.freedots.model.Fraction;

public class TestAugmentedFraction extends junit.framework.TestCase {
  public void testDotInference() {
    AugmentedFraction af;
    Fraction f;

    f = new Fraction(2, 1);
    af = new AugmentedFraction(f);
    assertEquals("numerator of "+f, af.getNumerator(), 2);
    assertEquals("denominator of "+f, af.getDenominator(), 1);
    assertEquals("dots of "+f, af.getDots(), 0);

    f = new Fraction(3, 1);
    af = new AugmentedFraction(f);
    assertEquals("nominator of "+f, af.getNumerator(), 2);
    assertEquals("denominator of "+f, af.getDenominator(), 1);
    assertEquals("dots of "+f, af.getDots(), 1);

    f = new Fraction(7, 2);
    af = new AugmentedFraction(f);
    assertEquals("numerator of "+f, af.getNumerator(), 2);
    assertEquals("denominator of "+f, af.getDenominator(), 1);
    assertEquals("dots of "+f, af.getDots(), 2);

    f = new Fraction(15, 4);
    af = new AugmentedFraction(new Fraction(15, 4));
    assertEquals("numerator of "+f, af.getNumerator(), 2);
    assertEquals("denominator of "+f, af.getDenominator(), 1);
    assertEquals("dots of "+f, af.getDots(), 3);

    f = new Fraction(31, 8);
    af = new AugmentedFraction(f);
    assertEquals("numerator of "+f, af.getNumerator(), 2);
    assertEquals("denominator of "+f, af.getDenominator(), 1);
    assertEquals("dots of "+f, af.getDots(), 4);


    f = new Fraction(1, 1);
    af = new AugmentedFraction(f);
    assertEquals("numerator of "+f, af.getNumerator(), 1);
    assertEquals("denominator of "+f, af.getDenominator(), 1);
    assertEquals("dots of "+f, af.getDots(), 0);

    f = new Fraction(3, 2);
    af = new AugmentedFraction(f);
    assertEquals("nominator of "+f, af.getNumerator(), 1);
    assertEquals("denominator of "+f, af.getDenominator(), 1);
    assertEquals("dots of "+f, af.getDots(), 1);

    f = new Fraction(7, 4);
    af = new AugmentedFraction(f);
    assertEquals("numerator of "+f, af.getNumerator(), 1);
    assertEquals("denominator of "+f, af.getDenominator(), 1);
    assertEquals("dots of "+f, af.getDots(), 2);

    f = new Fraction(15, 8);
    af = new AugmentedFraction(f);
    assertEquals("numerator of "+f, af.getNumerator(), 1);
    assertEquals("denominator of "+f, af.getDenominator(), 1);
    assertEquals("dots of "+f, af.getDots(), 3);

    f = new Fraction(31, 16);
    af = new AugmentedFraction(f);
    assertEquals("numerator of "+f, af.getNumerator(), 1);
    assertEquals("denominator of "+f, af.getDenominator(), 1);
    assertEquals("dots of "+f, af.getDots(), 4);


    f = new Fraction(1, 2);
    af = new AugmentedFraction(f);
    assertEquals("numerator of "+f, af.getNumerator(), 1);
    assertEquals("denominator of "+f, af.getDenominator(), 2);
    assertEquals("dots of "+f, af.getDots(), 0);

    f = new Fraction(3, 4);
    af = new AugmentedFraction(f);
    assertEquals("nominator of "+f, af.getNumerator(), 1);
    assertEquals("denominator of "+f, af.getDenominator(), 2);
    assertEquals("dots of "+f, af.getDots(), 1);

    f = new Fraction(7, 8);
    af = new AugmentedFraction(f);
    assertEquals("numerator of "+f, af.getNumerator(), 1);
    assertEquals("denominator of "+f, af.getDenominator(), 2);
    assertEquals("dots of "+f, af.getDots(), 2);

    f = new Fraction(15, 16);
    af = new AugmentedFraction(f);
    assertEquals("numerator of "+f, af.getNumerator(), 1);
    assertEquals("denominator of "+f, af.getDenominator(), 2);
    assertEquals("dots of "+f, af.getDots(), 3);

    f = new Fraction(31, 32);
    af = new AugmentedFraction(f);
    assertEquals("numerator of "+f, af.getNumerator(), 1);
    assertEquals("denominator of "+f, af.getDenominator(), 2);
    assertEquals("dots of "+f, af.getDots(), 4);

    f = new Fraction(1, 4);
    af = new AugmentedFraction(1, 4, 1);
    assertFalse("1/4 != 1/4.", f.equals(af));

    f = new Fraction(1, 24);
    af = new AugmentedFraction(1, 16, 0, 4, 6);
    assertTrue("1/16 (6 in 4) == 1/24", f.equals(af));
    assertEquals("1/16 (6 in 4) at 48 divisions", af.toInteger(48), 8);
  }
}
