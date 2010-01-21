package freedots.braille;

public class GuideDot extends Atom {
  GuideDot() { super(braille(3)); }
  public String getDescription() {
    return "Separates the previous from the following sign.";
  }
}
