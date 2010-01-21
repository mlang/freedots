package freedots.braille;

public class Dot extends Atom {
  Dot() { super(braille(3)); }
  public String getDescription() { return "Prolongation dot."; }
}
