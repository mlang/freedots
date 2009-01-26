/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots;

public enum Braille {
  dot(3), flat(126), natural(16), sharp(146), numberSign(3456),
  fullMeasureInAccord(126, 345),
  octave1(4, 4), octave2(4), octave3(45), octave4(456), octave5(5),
  octave6(46), octave7(56), octave8(6), octave9(6, 6),

  second(34), third(346), fourth(3456), fifth(35), sixth(356), seventh(25),
  octave(36),

  rightHandPart(46, 345), leftHandPart(456,345),

  digit0(245), digit1(1), digit2(12), digit3(14), digit4(145),
  digit5(15), digit6(124), digit7(1245), digit8(125), digit9(24),
  lowerDigit0(356), lowerDigit1(2), lowerDigit2(23), lowerDigit3(25),
  lowerDigit4(256), lowerDigit5(26), lowerDigit6(235), lowerDigit7(2356),
  lowerDigit8(236), lowerDigit9(35);

  static private Braille[] octaves = new Braille[] { octave1, octave2, octave3, octave4, octave5, octave6, octave7, octave8, octave9 };
  static private Braille[] digits = { digit0, digit1, digit2, digit3, digit4, digit5, digit6, digit7, digit8, digit9 };
  static private Braille[] lowerDigits = { lowerDigit0, lowerDigit1, lowerDigit2, lowerDigit3, lowerDigit4, lowerDigit5, lowerDigit6, lowerDigit7, lowerDigit8, lowerDigit9 };
  static private Braille[] intervals = { second, third, fourth, fifth, sixth, seventh, octave };

  int[] dots;
  Braille(int dots) { this.dots = new int[] {dots}; }
  Braille(int dots1, int dots2) { this.dots = new int[] {dots1, dots2}; }
  public String toString() {
    String result = "";
    for (int index = 0; index < dots.length; index++)
      result += String.valueOf(unicodeBraille(dotsToBits(dots[index])));
    return result;
  }

  static public Braille octave(int number) { return octaves[number]; }
  static public Braille upperDigit(int digit) { return digits[digit]; }
  static public Braille lowerDigit(int digit) { return lowerDigits[digit]; }
  static public Braille interval(int interval) { return intervals[interval - 1]; }

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

