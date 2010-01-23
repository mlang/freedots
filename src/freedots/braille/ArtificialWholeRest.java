package freedots.braille;

import freedots.music.AugmentedFraction;

public class ArtificialWholeRest extends BrailleList {
  public ArtificialWholeRest() {
    super();
    add(new Dot5());
    add(new RestSign(new AugmentedFraction(1, 1, 0)));
  }

  @Override public String getDescription() {
    return "Indicates that a whole measure is without any chord symbols";
  }

  public static class Dot5 extends Atom {
    Dot5() { super(braille(5)); }
    public String getDescription() {
      return "Signifies that the following sign has been added for "
             + "clarity but does not exist in the original print";
    }
  }
}
