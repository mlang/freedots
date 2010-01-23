package freedots.braille;

public class HarmonyPart extends NeedsGuideDot {
  public HarmonyPart() { super(braille(25, 345)); }

  public String getDescription() {
    return "Indicates that harmony (chordsymbols) are going to follow.";
  }
}
