package freedots.braille;

public class TieSign extends Atom {
  public TieSign() { super(braille(4, 14)); }

  public String getDescription() {
    return "Indicates that the previous and next note are tied";
  }
}
