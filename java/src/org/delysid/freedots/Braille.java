/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots;

public enum Braille {
  dot(3), flat(126), natural(16), sharp(146), numberSign(3456),
  fullVoiceSeparator(126, 345);

  int[] dots;
  Braille(int dots) { this.dots = new int[] {dots}; }
  Braille(int dots1, int dots2) { this.dots = new int[] {dots1, dots2}; }
  public String toString() {
    String result = "";
    for (int index = 0; index < dots.length; index++)
      result += String.valueOf(unicodeBraille(dotsToBits(dots[index])));
    return result;
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

