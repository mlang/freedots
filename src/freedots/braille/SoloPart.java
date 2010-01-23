package freedots.braille;

public class SoloPart extends NeedsGuideDot {
  public SoloPart() { super(braille(5, 345)); }

  public String getDescription() {
    return "Indicates music written on one staff.";
  }
}
