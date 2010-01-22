package freedots.braille;

import freedots.music.Accidental;

public class AccidentalSign extends Atom {
  private final Accidental accidental;

  AccidentalSign(final Accidental accidental) {
    super(getSign(accidental));
    this.accidental = accidental;
  }

  public String getDescription() {
    return accidental.toString();
  }

  private static String getSign(final Accidental accidental) {
    switch (accidental) {
    case SHARP:        return braille(146);
    case DOUBLE_SHARP: return braille(146, 146);
    case FLAT:         return braille(126);
    case DOUBLE_FLAT:  return braille(126, 126);
    case NATURAL:      return braille(16);
    default: throw new AssertionError(accidental);
    }
  }
}
