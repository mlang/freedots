package freedots.braille;

import freedots.music.AbstractPitch;
import freedots.music.AugmentedFraction;

public class ValueAndPitch extends Atom {
  private final AbstractPitch pitch;
  private final AugmentedFraction value;

  ValueAndPitch(final AugmentedFraction value, final AbstractPitch pitch) {
    super(getSign(value, pitch));
    this.value = value;
    this.pitch = pitch;
  }

  public String getDescription() {
    return "A " + pitch.toString() + " with duration " + value.toString();
  }
  private static String getSign(final AugmentedFraction value,
				final AbstractPitch pitch) {
    final int log = value.getLog();
    // FIXME: breve and long notes are not handled at all
    final int valueType = log > AugmentedFraction.EIGHTH
      ? log-AugmentedFraction.SIXTEENTH : log-2;

    final int[] stepDots = { 145, 15, 124, 1245, 125, 24, 245 };
    final int[] denomDots = { 36, 3, 6, 0 };
    return String.valueOf((char)(0X2800
                                 | dotsToBits(stepDots[pitch.getStep()])
				 | dotsToBits(denomDots[valueType])));
  }
}
