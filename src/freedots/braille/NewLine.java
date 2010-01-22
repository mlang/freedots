package freedots.braille;

public class NewLine extends Atom {
  public NewLine() { super(LINE_SEPARATOR); }
  public String getDescription() { return "Starts a new line."; }

  private static final String LINE_SEPARATOR =
    System.getProperty("line.separator");
}
