package freedots.braille;

public class LeftHandPart extends Atom {
  public LeftHandPart() { super(braille(46, 345)); }

  public String getDescription() {
    return "Indicates music for the left hand (second staff) of a keyboard instrument.";
  }
  public boolean mightNeedAdditionalDot() { return true; }
}
