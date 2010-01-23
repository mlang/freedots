package freedots.braille;

public class UpperNumber extends BrailleList {
  private final int number;

  public UpperNumber(int number) {
    super();
    this.number = number;
    add(new NumberSign());
    add(new UpperDigits(number));
  }

  @Override public String getDescription() {
    return "The number " + number + " with a number sign prefix";
  }
}
