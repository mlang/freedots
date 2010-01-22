package freedots.braille;

public class SlurSign extends Atom {
  public SlurSign() { super(braille(14)); }

  public String getDescription() {
    return "Indicates that the previous and next note are slurred";
  }
}
