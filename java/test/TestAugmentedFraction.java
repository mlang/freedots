import junit.framework.TestCase;

import org.delysid.freedots.model.AugmentedFraction;
import org.delysid.freedots.model.Fraction;

public class TestAugmentedFraction extends TestCase {
  public void testDotInference() {
    AugmentedFraction af = new AugmentedFraction(new Fraction(3, 4));
    assertTrue("3/4 => 1", af.getNumerator() == 1);
    assertTrue("3/4 => 1/2", af.getDenominator() == 2);
    assertTrue("3/4 => 1/2.", af.getDots() == 1);

    af = new AugmentedFraction(new Fraction(7, 8));
    assertTrue("7/8 => 1", af.getNumerator() == 1);
    assertTrue("7/8 => 1/2", af.getDenominator() == 2);
    assertTrue("7/8 => 1/2..", af.getDots() == 2);
  }
}
