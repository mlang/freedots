package freedots.braille;

public abstract class NeedsGuideDot extends Atom {
  NeedsGuideDot(String braille) { super(braille); }

  @Override public boolean needsGuideDot(BrailleSequence next) {
    if (next.length() > 0) {
      char ch = next.charAt(0);
      if (((int)ch & 0X2807) > 0X2800) return true;
    }

    return false;
  }
}
