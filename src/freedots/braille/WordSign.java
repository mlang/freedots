package freedots.braille;

public class WordSign extends Atom {
  public WordSign() { super(braille(345)); }

  public String getDescription() {
    return "Indicates that text is following, for dynamics or directions.";
  }
}
