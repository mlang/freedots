package freedots.braille;

public class MusicPart extends Atom {
  public MusicPart() { super(braille(6, 3)); }

  public String getDescription() {
    return "Indicates that music is going to follow.";
  }
}
