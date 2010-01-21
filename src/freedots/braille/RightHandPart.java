package freedots.braille;

public class RightHandPart extends Atom {
  public RightHandPart() { super(braille(456, 345)); }

  public String getDescription() {
    return "Indicates music for the right hand (first staff) of a keyboard instrument.";
  }
  public boolean mightNeedAdditionalDot() { return true; }
}
