package freedots.braille;

public class UpperDigits extends BrailleList {
  private final int number;

  UpperDigits(int number) {
    super();
    this.number = number;

    while (number > 0) {
      final int digit = number % 10;
      addFirst(new UpperDigit(digit));
      number = number / 10;
    }
  }

  @Override public String getDescription() {
    return "The number " + number;
  }
}
