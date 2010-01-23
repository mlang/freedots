package freedots.braille;

public class MusicHyphen extends Atom {
  public MusicHyphen() { super(braille(5)); }

  public String getDescription() {
    return "Indicates that this measure is continued on the next line or page.";
  }
}
