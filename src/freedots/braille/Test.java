package freedots.braille;

public class Test {
  public static final void main(String[] args) {
    Compound braille = new Compound();
    braille.add(new RightHandPart());
    braille.add(new BrailleNote(0, 3, 0, 1));
    braille.add(new Space());
    braille.add(new OctaveSign(1));
    braille.add(new NewLine());
    System.out.println(braille.toString() + " = " + braille.getDescription());
    for (int i = 0; i < braille.length(); i++) {
      final Sign sign = braille.getSignAtIndex(i);
      final Object object = braille.getScoreObjectAtIndex(i);
      System.out.println("i = "+i+": '"+sign+"' = "+sign.getDescription());
      System.out.println("  "+object);
      if (object != null)
        System.out.println("   i = "+braille.getIndexOfScoreObject(object));
    }
  }
}
