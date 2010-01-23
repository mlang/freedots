package freedots.braille;

public class LeftHandPart extends NeedsGuideDot {
  public LeftHandPart() { super(braille(456, 345)); }

  public String getDescription() {
    return "Indicates music for the left hand (second staff) of a keyboard instrument.";
  }
}
