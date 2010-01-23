package freedots.braille;

public class TextPart extends Atom {
  public TextPart() { super(braille(56, 23)); }

  public String getDescription() {
    return "Indicates that text (lyrics) is going to follow.";
  }
}
