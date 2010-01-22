package freedots.braille;

public class UpperDigit extends Atom {
  private final int digit;

  UpperDigit(int digit) {
    super(getSign(digit));
    this.digit = digit;
  }

  @Override public String getDescription() {
    return "The digit " + digit;
  }

  private static String getSign(final int digit) {
    return DIGITS[digit];
  }

  private static final String[] DIGITS = new String[] {
    braille(245),
    braille(1), braille(12), braille(14),
    braille(145), braille(15), braille(124),
    braille(1245), braille(125), braille(24)
  };
}
