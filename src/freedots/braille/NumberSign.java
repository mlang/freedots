package freedots.braille;

public class NumberSign extends Atom {
  NumberSign() { super(braille(3456)); }

  public String getDescription() {
    return "Indicates the beginning of a number.";
  }
}
