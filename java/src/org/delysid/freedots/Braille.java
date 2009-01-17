/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots;

public enum Braille {
  dot(3), flat(126), natural(16), sharp(146), numberSign(3456),
  fullVoiceSeparator(126, 345),
  octave1(4, 4), octave2(4), octave3(45), octave4(456), octave5(5),
  octave6(46), octave7(56), octave8(6), octave9(6, 6);

  static private Braille[] octaves = new Braille[] { octave1, octave2, octave3, octave4, octave5, octave6, octave7, octave8, octave9 };

  int[] dots;
  Braille(int dots) { this.dots = new int[] {dots}; }
  Braille(int dots1, int dots2) { this.dots = new int[] {dots1, dots2}; }
  public String toString() {
    String result = "";
    for (int index = 0; index < dots.length; index++)
      result += String.valueOf(unicodeBraille(dotsToBits(dots[index])));
    return result;
  }

  static public Braille octave(int number) {
    return octaves[number];
  }
  static public char unicodeBraille(int bits) {
    return (char)(0X2800 | bits);
  }
  static public int dotsToBits(int dots) {
    int bits = 0;
    while (dots > 0) {
      int number = dots % 10;
      dots /= 10;
      bits |= 1 << (number - 1);
    }
    return bits;
  }
}

