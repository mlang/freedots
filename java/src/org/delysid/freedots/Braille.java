/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots;

public enum Braille {
  dot(3), flat(126), natural(16), sharp(146), 

  valueDistinction(126, 2),

  numberSign(3456),

  tie(4, 14), slur(14),

  grace(5, 26), mordent(5, 235, 123), turn(6, 256),

  fullMeasureInAccord(126, 345),
  partMeasureInAccord(46, 13), partMeasureInAccordDivision(5, 2),

  octave1(4, 4), octave2(4), octave3(45), octave4(456), octave5(5),
  octave6(46), octave7(56), octave8(6), octave9(6, 6),

  second(34), third(346), fourth(3456), fifth(35), sixth(356), seventh(25),
  octave(36),

  rightHandPart(46, 345), leftHandPart(456, 345),

  hyphen(5),

  postDottedDoubleBar(126, 2356), dottedDoubleBar(126, 23), doubleBar(126, 13),

  finger1(1), finger2(12), finger3(123), finger4(2), finger5(13),

  digit0(245), digit1(1), digit2(12), digit3(14), digit4(145),
  digit5(15), digit6(124), digit7(1245), digit8(125), digit9(24),
  lowerDigit0(356), lowerDigit1(2), lowerDigit2(23), lowerDigit3(25),
  lowerDigit4(256), lowerDigit5(26), lowerDigit6(235), lowerDigit7(2356),
  lowerDigit8(236), lowerDigit9(35);

  private final static Braille[] octaves = new Braille[] { octave1, octave2, octave3, octave4, octave5, octave6, octave7, octave8, octave9 };
  private final static Braille[] digits = { digit0, digit1, digit2, digit3, digit4, digit5, digit6, digit7, digit8, digit9 };
  private final static Braille[] lowerDigits = { lowerDigit0, lowerDigit1, lowerDigit2, lowerDigit3, lowerDigit4, lowerDigit5, lowerDigit6, lowerDigit7, lowerDigit8, lowerDigit9 };
  private final static Braille[] intervals = { second, third, fourth, fifth, sixth, seventh, octave };
  private final static Braille[] fingers = { finger1, finger2, finger3, finger4, finger5 };

  private int[] dots;
  Braille(int dots) { this.dots = new int[] {dots}; }
  Braille(int dots1, int dots2) { this.dots = new int[] {dots1, dots2}; }
  Braille(int dots1, int dots2, int dots3) {
    this.dots = new int[] {dots1, dots2, dots3};
  }
  @Override
  public String toString() {
    String result = "";
    for (int element : dots)
      result += String.valueOf(unicodeBraille(dotsToBits(element)));
    return result;
  }

  public static Braille octave(int number) { return octaves[number]; }
  public static Braille upperDigit(int digit) { return digits[digit]; }
  public static Braille lowerDigit(int digit) { return lowerDigits[digit]; }
  public static Braille interval(int interval) { return intervals[interval - 1]; }
  public static Braille finger(int finger) { return fingers[finger - 1]; }

  public static String nTimes(Braille item, int count) {
    if (count <= 0) return "";
    else if (count == 1) return item.toString();
    else if (count == 2) return item.toString()+item.toString();
    else if (count == 3) return item.toString()+item.toString()+item.toString();
    else if (count >= 4 && count < 10)
      return numberSign.toString()+upperDigit(count).toString()+item;
    else return "ERR";
  }
  public static char unicodeBraille(int bits) {
    return (char)(0X2800 | bits);
  }
  public static int dotsToBits(int dots) {
    int bits = 0;
    while (dots > 0) {
      int number = dots % 10;
      dots /= 10;
      bits |= 1 << (number - 1);
    }
    return bits;
  }
}
