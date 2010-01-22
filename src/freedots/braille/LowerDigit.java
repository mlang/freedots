package freedots.braille;

public class LowerDigit extends Atom {
  private final int digit;

  LowerDigit(int digit) {
    super(getSign(digit));
    this.digit = digit;
  }

  @Override public String getDescription() {
    return "The digit " + digit + " in the lower part of a braille cell";
  }

  private static String getSign(final int digit) {
    return DIGITS[digit];
  }

  private static final String[] DIGITS = new String[] {
    braille(356),
    braille(2), braille(23), braille(25),
    braille(256), braille(26), braille(235),
    braille(2356), braille(236), braille(35)
  };
}
