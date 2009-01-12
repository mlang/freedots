/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots;

public class Braille {
  static char unicodeBraille(int bits) {
    return (char)(0X2800 | bits);
  }
  static int dotsToBits(int dots) {
    int bits = 0;
    while (dots > 0) {
      int number = dots % 10;
      dots /= 10;
      bits |= 1 << (number - 1);
    }
    return bits;
  }
}

