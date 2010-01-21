package freedots.braille;

public class AccidentalSign extends Atom {
  private final float alter;

  AccidentalSign(float alter) {
    super(getSignFromAlter(alter));
    this.alter = alter;
  }

  public String getDescription() {
    if (alter == 1) return "sharp";
    else if (alter == 2) return "double sharp";
    else if (alter == -1) return "flat.";
    else if (alter == -2) return "double flat.";
    else throw new AssertionError(alter);
  }
  private static String getSignFromAlter(float alter) {
    if (alter == 1) return braille(146);
    else if (alter == 2) return braille(146, 146);
    else if (alter == -1) return braille(126);
    else if (alter == -2) return braille(126, 126);
    else if (alter == 0) return braille(16);
    else throw new AssertionError(alter);
  }
}
