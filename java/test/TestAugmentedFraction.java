import org.delysid.freedots.model.AugmentedFraction;
import org.delysid.freedots.model.Fraction;

public class TestAugmentedFraction extends junit.framework.TestCase {
  public void testDotInference() {
    AugmentedFraction af;

    af = new AugmentedFraction(new Fraction(2, 1));
    assertEquals("numerator of 2", af.getNumerator(), 2);
    assertEquals("denominator of 2", af.getDenominator(), 1);
    assertEquals("dots of 2", af.getDots(), 0);

    af = new AugmentedFraction(new Fraction(3, 1));
    assertEquals("nominator of 3", af.getNumerator(), 2);
    assertEquals("denominator of 3", af.getDenominator(), 1);
    assertEquals("dots of 3", af.getDots(), 1);

    af = new AugmentedFraction(new Fraction(7, 2));
    assertEquals("numerator of 7/2", af.getNumerator(), 2);
    assertEquals("denominator of 7/2", af.getDenominator(), 1);
    assertEquals("dots of 7/2", af.getDots(), 2);

    af = new AugmentedFraction(new Fraction(15, 4));
    assertEquals("numerator of 15/4", af.getNumerator(), 2);
    assertEquals("denominator of 15/4", af.getDenominator(), 1);
    assertEquals("dots of 15/4", af.getDots(), 3);

    af = new AugmentedFraction(new Fraction(31, 8));
    assertEquals("numerator of 31/8", af.getNumerator(), 2);
    assertEquals("denominator of 31/8", af.getDenominator(), 1);
    assertEquals("dots of 31/8", af.getDots(), 4);


    af = new AugmentedFraction(new Fraction(1, 1));
    assertEquals("numerator of 1", af.getNumerator(), 1);
    assertEquals("denominator of 1", af.getDenominator(), 1);
    assertEquals("dots of 1", af.getDots(), 0);

    af = new AugmentedFraction(new Fraction(3, 2));
    assertEquals("nominator of 3/2", af.getNumerator(), 1);
    assertEquals("denominator of 3/2", af.getDenominator(), 1);
    assertEquals("dots of 3/2", af.getDots(), 1);

    af = new AugmentedFraction(new Fraction(7, 4));
    assertEquals("numerator of 7/4", af.getNumerator(), 1);
    assertEquals("denominator of 7/4", af.getDenominator(), 1);
    assertEquals("dots of 7/4", af.getDots(), 2);

    af = new AugmentedFraction(new Fraction(15, 8));
    assertEquals("numerator of 15/8", af.getNumerator(), 1);
    assertEquals("denominator of 15/8", af.getDenominator(), 1);
    assertEquals("dots of 15/8", af.getDots(), 3);

    af = new AugmentedFraction(new Fraction(31, 16));
    assertEquals("numerator of 31/16", af.getNumerator(), 1);
    assertEquals("denominator of 31/16", af.getDenominator(), 1);
    assertEquals("dots of 31/16", af.getDots(), 4);


    af = new AugmentedFraction(new Fraction(1, 2));
    assertEquals("numerator of 1/2", af.getNumerator(), 1);
    assertEquals("denominator of 1/2", af.getDenominator(), 2);
    assertEquals("dots of 1/2", af.getDots(), 0);

    af = new AugmentedFraction(new Fraction(3, 4));
    assertEquals("nominator of 3/4", af.getNumerator(), 1);
    assertEquals("denominator of 3/4", af.getDenominator(), 2);
    assertEquals("dots of 3/4", af.getDots(), 1);

    af = new AugmentedFraction(new Fraction(7, 8));
    assertEquals("numerator of 7/8", af.getNumerator(), 1);
    assertEquals("denominator of 7/8", af.getDenominator(), 2);
    assertEquals("dots of 7/8", af.getDots(), 2);

    af = new AugmentedFraction(new Fraction(15, 16));
    assertEquals("numerator of 15/16", af.getNumerator(), 1);
    assertEquals("denominator of 15/16", af.getDenominator(), 2);
    assertEquals("dots of 15/16", af.getDots(), 3);

    af = new AugmentedFraction(new Fraction(31, 32));
    assertEquals("numerator of 31/32", af.getNumerator(), 1);
    assertEquals("denominator of 31/32", af.getDenominator(), 2);
    assertEquals("dots of 31/32", af.getDots(), 4);
  }
}
