package freedots.braille;

public class LowerNumber extends BrailleList {
  private final int number;

  LowerNumber(int number) {
    super();
    this.number = number;
    add(new NumberSign());
    add(new LowerDigits(number));
  }

  @Override public String getDescription() {
    return "The number " + number + " with a number sign prefix and written as lowered digits";
  }
}
