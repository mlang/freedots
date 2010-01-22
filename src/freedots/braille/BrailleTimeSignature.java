package freedots.braille;

import freedots.music.TimeSignature;

public class BrailleTimeSignature extends BrailleList {
  private final TimeSignature timeSignature;

  BrailleTimeSignature(TimeSignature timeSignature) {
    super();
    this.timeSignature = timeSignature;

    add(new NumberSign());
    add(new UpperDigits(timeSignature.getNumerator()));
    add(new LowerDigits(timeSignature.getDenominator()));
  }

  @Override public String getDescription() {
    return "A " + timeSignature.toString() + " time signature";
  }
  @Override public Object getScoreObject() { return timeSignature; }
}
