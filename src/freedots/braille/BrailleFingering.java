package freedots.braille;

import java.util.Iterator;

import freedots.music.Fingering;

public class BrailleFingering extends BrailleList {
  private final Fingering fingering;

  public BrailleFingering(final Fingering fingering) {
    super();
    this.fingering = fingering;

    Iterator<Integer> iter = fingering.getFingers().iterator();
    while (iter.hasNext()) {
      add(new Finger(iter.next()));
      if (iter.hasNext())
        add(new SlurSign() {
              @Override public String getDescription() {
                return "Indicates a silent finger change";
              }
            });
    }
  }

  @Override public String getDescription() {
    return "Fingering: " + fingering.toString();
  }

  public static class Finger extends Atom {
    private final int finger;

    Finger(final int finger) {
      super(getSign(finger));
      this.finger = finger;
    }
    public String getDescription() {
      return "Indicates that finger " + finger + " should be used";
    }
    private static String getSign(final int number) {
      return FINGERS[number - 1];
    }
    private static String[] FINGERS = new String[] {
      braille(1), braille(12), braille(123), braille(4), braille(13)
    };
  }
}
