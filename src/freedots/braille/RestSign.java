package freedots.braille;

import freedots.music.AbstractPitch;
import freedots.music.AugmentedFraction;

public class RestSign extends Atom {
  private final AugmentedFraction value;

  RestSign(final AugmentedFraction value) {
    super(getSign(value));
    this.value = value;
  }

  public String getDescription() {
    return "A rest with duration " + value.toString();
  }
  private static String getSign(final AugmentedFraction value) {
    final int log = value.getLog();
    // FIXME: breve and long notes are not handled at all
    final int valueType = log > AugmentedFraction.EIGHTH
      ? log-AugmentedFraction.SIXTEENTH : log-2;

    final int[] restDots = { 134, 136, 1236, 1346 };
    return braille(restDots[valueType]);
  }
}
