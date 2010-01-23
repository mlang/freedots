package freedots.braille;

public class DoubleBarSign extends Atom {
  public DoubleBarSign() { super(braille(126, 13)); }

  public String getDescription() {
    return "Signifies the end of the music";
  }
}
