package freedots.braille;

public class GraceSign extends Atom {
  GraceSign() { super(braille(5, 26)); }

  public String getDescription() {
    return "Indicates the this is a grace note.";
  }
}
