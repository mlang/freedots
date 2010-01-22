package freedots.braille;

import freedots.music.TimeSignature;

public class Test {
  public static final void main(String[] args) {
    BrailleList braille = new BrailleList();
    braille.add(new BrailleTimeSignature(new TimeSignature(9, 16)));
    braille.add(new NewLine());
    braille.add(new RightHandPart());
    braille.add(new Space());
    braille.add(new OctaveSign(1));
    System.out.println(braille.toString() + " = " + braille.getDescription());
    for (int i = 0; i < braille.length(); i++) {
      final Atom sign = braille.getSignAtIndex(i);
      final Object object = braille.getScoreObjectAtIndex(i);
      System.out.println("i = "+i+": '"+sign+"' = "+sign.getDescription());
      System.out.println("  "+object);
      if (object != null)
        System.out.println("   i = "+braille.getIndexOfScoreObject(object));
    }

  }
}
