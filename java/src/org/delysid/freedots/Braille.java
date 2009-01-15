/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots;

public enum Braille {
  dot(3), numberSign(3456);

  int dots;
  Braille(int dots) { this.dots = dots; }
  public String toString() {
    return String.valueOf(unicodeBraille(dotsToBits(dots)));
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

