package freedots.braille;

public class OctaveSign extends Atom {
  private final int octave;
  public OctaveSign(final int octave) {
    super(getBraille(octave));
    this.octave = octave;
  }
  public String getDescription () {
    return new StringBuilder()
      .append("Indicates that the following note belongs to the ")
      .append(OCTAVE_NAMES[octave]).append(" octave.").toString();
  } 

  private static String getBraille(final int octave) {
    return OCTAVE_SIGNS[octave];
  }
  private static final String[] OCTAVE_SIGNS = new String[] {
    braille(4, 4), braille(4), braille(45), braille(456), braille(5),
    braille(46), braille(56), braille(6), braille(6, 6)
  };
  private static final String[] OCTAVE_NAMES = new String[] {
    "subsubcontra", "sub-contra", "contra", "great", "small",
    "one-lined", "two-lined", "three-lined", "four-lined",
    "five-lined", "six-lined"
  };
}
