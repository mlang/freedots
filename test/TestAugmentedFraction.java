/* -*- c-basic-offset: 2; -*- */

import java.util.List;

import freedots.math.Fraction;
import freedots.music.AugmentedPowerOfTwo;

public class TestAugmentedFraction extends junit.framework.TestCase {
  public void testDotInference() {
    AugmentedPowerOfTwo af;
    Fraction f;

    f = new Fraction(2, 1);
    af = AugmentedPowerOfTwo.valueOf(f);
    assertEquals("numerator of "+f, af.numerator(), 2);
    assertEquals("denominator of "+f, af.denominator(), 1);
    assertEquals("dots of "+f, af.dots(), 0);

    f = new Fraction(3, 1);
    af = AugmentedPowerOfTwo.valueOf(f);
    assertEquals("power of "+f, af.getPower(), 1);
    assertEquals("dots of "+f, af.dots(), 1);

    f = new Fraction(7, 2);
    af = AugmentedPowerOfTwo.valueOf(f);
    assertEquals("power of "+f, af.getPower(), 1);
    assertEquals("dots of "+f, af.dots(), 2);

    f = new Fraction(15, 4);
    af = AugmentedPowerOfTwo.valueOf(new Fraction(15, 4));
    assertEquals("power of "+f, af.getPower(), 1);
    assertEquals("dots of "+f, af.dots(), 3);

    f = new Fraction(31, 8);
    af = AugmentedPowerOfTwo.valueOf(f);
    assertEquals("power of "+f, af.getPower(), 1);
    assertEquals("dots of "+f, af.dots(), 4);

    f = new Fraction(1, 1);
    af = AugmentedPowerOfTwo.valueOf(f);
    assertEquals("power of "+f, af.getPower(), 0);
    assertEquals("dots of "+f, af.dots(), 0);

    f = new Fraction(3, 2);
    af = AugmentedPowerOfTwo.valueOf(f);
    assertEquals("power of "+f, af.getPower(), 0);
    assertEquals("dots of "+f, af.dots(), 1);

    f = new Fraction(7, 4);
    af = AugmentedPowerOfTwo.valueOf(f);
    assertEquals("power of "+f, af.getPower(), 0);
    assertEquals("dots of "+f, af.dots(), 2);

    f = new Fraction(15, 8);
    af = AugmentedPowerOfTwo.valueOf(f);
    assertEquals("numerator of "+f, af.getPower(), 0);
    assertEquals("dots of "+f, af.dots(), 3);

    f = new Fraction(31, 16);
    af = AugmentedPowerOfTwo.valueOf(f);
    assertEquals("power of "+f, af.getPower(), 0);
    assertEquals("dots of "+f, af.dots(), 4);

    f = new Fraction(1, 2);
    af = AugmentedPowerOfTwo.valueOf(f);
    assertEquals("power of "+f, af.getPower(), -1);
    assertEquals("dots of "+f, af.dots(), 0);

    f = new Fraction(3, 4);
    af = AugmentedPowerOfTwo.valueOf(f);
    assertEquals("power of "+f, af.getPower(), -1);
    assertEquals("dots of "+f, af.dots(), 1);

    f = new Fraction(7, 8);
    af = AugmentedPowerOfTwo.valueOf(f);
    assertEquals("power of "+f, af.getPower(), -1);
    assertEquals("dots of "+f, af.dots(), 2);

    f = new Fraction(15, 16);
    af = AugmentedPowerOfTwo.valueOf(f);
    assertEquals("numerator of "+f, af.getPower(), -1);
    assertEquals("dots of "+f, af.dots(), 3);

    f = new Fraction(31, 32);
    af = AugmentedPowerOfTwo.valueOf(f);
    assertEquals("power of "+f, af.getPower(), -1);
    assertEquals("dots of "+f, af.dots(), 4);

    f = new Fraction(1, 4);
    af = new AugmentedPowerOfTwo(AugmentedPowerOfTwo.CROTCHET, 1);
    assertFalse("1/4 != 1/4.", f.equals(af));

    f = new Fraction(1, 24);
    af = new AugmentedPowerOfTwo(AugmentedPowerOfTwo.SEMIQUAVER, 0, 4, 6);
    assertTrue("1/16 (6 in 4) == 1/24", f.equals(af));

    af = new AugmentedPowerOfTwo(AugmentedPowerOfTwo.QUAVER, 1);
    assertEquals("1/8. log == 5", af.getPower(), -3);
  }
  public void testList() {
    for (int n = 1; n <= 128; n++)
      for (int  d = 1; d <= 32; d = d<<1) {
        Fraction f = new Fraction(n, d).simplify();
        List<AugmentedPowerOfTwo> l =
	  AugmentedPowerOfTwo.decompose(f, AugmentedPowerOfTwo.LONGA);
        assertEquals(f.toString()+" = "+l.toString(), f, sum(l));
      }
  }
  private Fraction sum(List<AugmentedPowerOfTwo> list) {
    Fraction result = Fraction.ZERO;
    for (AugmentedPowerOfTwo af: list) result = result.add(af);
    return result;
  }
}
