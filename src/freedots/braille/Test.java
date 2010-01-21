package freedots.braille;

public class Test {
  public static final void main(String[] args) {
    Compound line = new Compound();
    line.add(new RightHandPart());
    line.add(new BrailleNote(0, 3, 0, 1));
    line.add(new Space());
    line.add(new OctaveSign(1));
    System.out.println(line.toString() + " = " + line.getDescription());
    for (int i = 0; i < line.length(); i++) {
      final Sign sign = line.getSignAtIndex(i);
      final Object object = line.getScoreObjectAtIndex(i);
      System.out.println("i = "+i+": '"+sign+"' = "+sign.getDescription());
      System.out.println("  "+object);
      if (object != null)
        System.out.println("   i = "+line.getIndexOfScoreObject(object));
    }
  }
}
