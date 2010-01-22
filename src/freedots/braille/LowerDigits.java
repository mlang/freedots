package freedots.braille;

public class LowerDigits extends BrailleList {
  private final int number;

  LowerDigits(int number) {
    super();
    this.number = number;

    while (number > 0) {
      final int digit = number % 10;
      addFirst(new LowerDigit(digit));
      number = number / 10;
    }
  }

  @Override public String getDescription() {
    return "The number " + number + " formatted with digits in the lower part of a braille cell";
  }
}
