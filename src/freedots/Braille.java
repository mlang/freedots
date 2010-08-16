/* -*- c-basic-offset: 2; indent-tabs-mode: nil; -*- */
/*
 * FreeDots -- MusicXML to braille music transcription
 *
 * Copyright 2008-2010 Mario Lang  All Rights Reserved.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 3, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details (a copy is included in the LICENSE.txt file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License
 * along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This file is maintained by Mario Lang <mlang@delysid.org>.
 */
package freedots;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** All the braille signs required for music and a few utility methods.
 * <p>
 * TODO: This class is an enum for purely historical reasons, there is actually
 * no real use of the properties of an enum except for the syntactic sugar
 * it offers for initialising static members.
 */
@Deprecated
public enum Braille {
  valueDistinction(126, 2),

  // Piano pedal marks
  pedalPress(126, 14), pedalRelease(16, 14), pedalChange(16, 126, 14),

  fermata(126, 123), fermataSquare(56, 126, 123), fermataTent(45, 126, 123);

  private int[] dots;
  private String cachedString;

  Braille(final int dots) { this(new int[] {dots}); }
  Braille(final int dots1, final int dots2) { this(new int[] {dots1, dots2}); }
  Braille(final int dots1, final int dots2, final int dots3) {
    this(new int[] {dots1, dots2, dots3});
  }
  private Braille(final int[] dots) {
    this.dots = dots;
    cachedString = "";
    for (int element: dots)
      cachedString += String.valueOf(unicodeBraille(dotsToBits(element)));
  }
  @Override
  public String toString() {
    return cachedString;
  }

  public static final Map<Character, Character> BRF_TABLE =
    Collections.unmodifiableMap(new HashMap<Character, Character>() {
      {
        put(createCharacter(0),     new Character(' '));
        put(createCharacter(1),     new Character('A'));
        put(createCharacter(2),     new Character('1'));
        put(createCharacter(3),     new Character((char)0X27));
        put(createCharacter(4),     new Character('@'));
        put(createCharacter(5),     new Character('"'));
        put(createCharacter(6),     new Character(','));
        put(createCharacter(12),    new Character('B'));
        put(createCharacter(13),    new Character('K'));
        put(createCharacter(14),    new Character('C'));
        put(createCharacter(15),    new Character('E'));
        put(createCharacter(16),    new Character('*'));
        put(createCharacter(23),    new Character('2'));
        put(createCharacter(24),    new Character('I'));
        put(createCharacter(25),    new Character('3'));
        put(createCharacter(26),    new Character('5'));
        put(createCharacter(34),    new Character('/'));
        put(createCharacter(35),    new Character('9'));
        put(createCharacter(36),    new Character('-'));
        put(createCharacter(45),    new Character('^'));
        put(createCharacter(46),    new Character('.'));
        put(createCharacter(56),    new Character(';'));
        put(createCharacter(123),   new Character('L'));
        put(createCharacter(124),   new Character('F'));
        put(createCharacter(125),   new Character('H'));
        put(createCharacter(126),   new Character('<'));
        put(createCharacter(134),   new Character('M'));
        put(createCharacter(135),   new Character('O'));
        put(createCharacter(136),   new Character('U'));
        put(createCharacter(145),   new Character('D'));
        put(createCharacter(146),   new Character('%'));
        put(createCharacter(156),   new Character(':'));
        put(createCharacter(234),   new Character('S'));
        put(createCharacter(235),   new Character('6'));
        put(createCharacter(236),   new Character('8'));
        put(createCharacter(245),   new Character('J'));
        put(createCharacter(246),   new Character('['));
        put(createCharacter(256),   new Character('4'));
        put(createCharacter(345),   new Character('>'));
        put(createCharacter(346),   new Character('+'));
        put(createCharacter(356),   new Character('0'));
        put(createCharacter(456),   new Character('_'));
        put(createCharacter(1234),  new Character('P'));
        put(createCharacter(1235),  new Character('R'));
        put(createCharacter(1236),  new Character('V'));
        put(createCharacter(1245),  new Character('G'));
        put(createCharacter(1246),  new Character('$'));
        put(createCharacter(1256),  new Character((char)0X5C));
        put(createCharacter(1345),  new Character('N'));
        put(createCharacter(1346),  new Character('X'));
        put(createCharacter(1356),  new Character('Z'));
        put(createCharacter(1456),  new Character('?'));
        put(createCharacter(2345),  new Character('T'));
        put(createCharacter(2346),  new Character('!'));
        put(createCharacter(2356),  new Character('7'));
        put(createCharacter(2456),  new Character('W'));
        put(createCharacter(3456),  new Character('#'));
        put(createCharacter(12345), new Character('Q'));
        put(createCharacter(12346), new Character('&'));
        put(createCharacter(12356), new Character('('));
        put(createCharacter(12456), new Character(']'));
        put(createCharacter(13456), new Character('Y'));
        put(createCharacter(23456), new Character(')'));
        put(createCharacter(123456), new Character('='));
      }
    });

  private static Character createCharacter(int dots) {
    return new Character(unicodeBraille(dotsToBits(dots)));
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
