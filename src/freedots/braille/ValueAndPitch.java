package freedots.braille;

public class ValueAndPitch extends Atom {
  private int value, step;

  ValueAndPitch(int value, int step) {
    super(getSign(value, step));
    this.value = value;
    this.step = step;
  }

  public String getDescription() {
    return "";
  }
  private static String getSign(int value, int step) {
    return braille(13456);
  }
}
