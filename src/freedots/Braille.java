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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import freedots.braille.AccidentalSign;
import freedots.logging.Logger;
import freedots.music.Accidental;
import freedots.music.AugmentedFraction;
import freedots.musicxml.Harmony;

/** All the braille signs required for music and a few utility methods.
 * <p>
 * TODO: This class is an enum for purely historical reasons, there is actually
 * no real use of the properties of an enum except for the syntactic sugar
 * it offers for initialising static members.
 */
@Deprecated
public enum Braille {
  dot(3), wholeRest(134),
  flat(126), natural(16), sharp(146),

  valueDistinction(126, 2),

  numberSign(3456), simileSign(2356),

  slur(14),

  // Piano pedal marks
  pedalPress(126, 14), pedalRelease(16, 14), pedalChange(16, 126, 14),

  fullMeasureInAccord(126, 345),
  partMeasureInAccord(46, 13), partMeasureInAccordDivision(5, 2),

  // Signs for announcing the type of "staff"
  rightHandPart(46, 345), soloPart(5, 345), leftHandPart(456, 345),
  harmonyPart(25, 345), musicPart(6, 3), textPart(56, 23),

  upcase(46),
  letterA(1), letterB(12), letterC(14), letterD(145), letterE(15), letterF(124),
  letterG(1245), slash(5, 2), letterM(134),
  artificialWholeRest(5, 134),

  // Stem signs are written after the notes (or chords) to which they belong
  wholeStem(456, 3), halfStem(456, 13),
  quarterStem(456, 1), eighthStem(456, 12), sixteenthStem(456, 123),
  thirtysecondthStem(456, 2),

  fermata(126, 123), fermataSquare(56, 126, 123), fermataTent(45, 126, 123),

  firstString(146, 1), secondString(146, 12), thirdString(146, 123),
  fourthString(146, 2), fifthString(146, 13), sixthString(146, 23),
  seventhString(146, 3),

  // Digits written in the upper part of a cell
  digit0(245), digit1(1), digit2(12), digit3(14), digit4(145),
  digit5(15), digit6(124), digit7(1245), digit8(125), digit9(24);

  private static final Logger LOG = Logger.getLogger(Braille.class);
  private int[] dots;
  private String cachedString;
  private boolean needsAdditionalDot3IfOneOfDot123Follows = false;

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

  /**
   * Concatenate a number of repetitions of this braille symbol according to
   * braille music rules.
   * @param amount number of repetitions
   * @return the concatenated repetition as a String
   */
  public String repeat(final int amount) {
    if (amount <= 0) return "";

    String atom = toString();
    if (amount == 1) return atom;
    else if (amount == 2) return atom + atom;
    else if (amount == 3) return atom + atom + atom;
    else {
      return numberSign.toString() + upperNumber(amount) + atom;
    }
  }

  /**
   * @return true if this braille music symbol needs an additional dot 3
   * if one of dots 1, 2 or 3 is following.
   */
  // TODO: Method name should be changed to something better!
  public boolean needsAdditionalDot3IfOneOfDot123Follows() {
    return needsAdditionalDot3IfOneOfDot123Follows;
  }
  private void needsAdditionalDot3IfOneOfDot123Follows(boolean newValue) {
    needsAdditionalDot3IfOneOfDot123Follows = newValue;
  }
  static {
    leftHandPart.needsAdditionalDot3IfOneOfDot123Follows(true);
    soloPart.needsAdditionalDot3IfOneOfDot123Follows(true);
    rightHandPart.needsAdditionalDot3IfOneOfDot123Follows(true);
    harmonyPart.needsAdditionalDot3IfOneOfDot123Follows(true);
    // TODO: There are probabbly more, figure out the complete list
  }

  /** Format a number using the upper dots 1, 2, 4 and 5.
   * @param number is the number to translate to braille
   * @return the unicode braille representation of the number
   */
  public static String upperNumber(int number) {
    String string = "";
    while (number > 0) {
      int digit = number % 10;
      string = DIGITS[digit] + string;
      number = number / 10;
    }
    return string;
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

  private static final Braille[] DIGITS = {
    digit0,
    digit1, digit2, digit3,
    digit4, digit5, digit6,
    digit7, digit8, digit9
  };
  private static final Braille[] ENGLISH_STEPS = {
    letterC, letterD, letterE, letterF, letterG, letterA, letterB
  };


  /** Formats a list of augmented musical fractions using stem and slur signs.
   * This method is typically used together with
   * {@link freedots.music.Fraction#decompose} for annotating chord symbols
   * with their duration.
   * @see #toString(Harmony)
   * @return a Unicode braille string
   */
  public static String toString(List<AugmentedFraction> afList) {
    StringBuilder sb = new StringBuilder();
    Iterator<AugmentedFraction> iterator = afList.iterator();
    while (iterator.hasNext()) {
      AugmentedFraction af = iterator.next();
      if (af.getNumerator() == 1) {
        if (af.getDenominator() == 1) sb.append(wholeStem);
        else if (af.getDenominator() == 2) sb.append(halfStem);
        else if (af.getDenominator() == 4) sb.append(quarterStem);
        else if (af.getDenominator() == 8) sb.append(eighthStem);
        else if (af.getDenominator() == 16) sb.append(sixteenthStem);
        else if (af.getDenominator() == 32) sb.append(thirtysecondthStem);
        else LOG.warning("Unmapped denominator: "+af);
      } else LOG.warning("Unmapped numerator: "+af.getNumerator());
      if (af.getDots() > 0) {
        for (int i = 0; i < af.getDots(); i++) sb.append(dot);
      }
      if (iterator.hasNext()) sb.append(slur);
    }
    return sb.toString();
  }

  /** Converts the given {@link freedots.musicxml.Harmony} instance to its
   *  braille representation.
   * @return a Unicode String with the braille music representation of the
   * given chord symbol.
   */
  public static String toString(Harmony harmony) {
    StringBuilder sb = new StringBuilder();
    for (Harmony.HarmonyChord chord: harmony.getChords()) {
      String kind = chord.getKind();
      if ("none".equals(kind)) {
        sb.append(upcase).append("nc");
      } else {
        sb.append(upcase).append(ENGLISH_STEPS[chord.getRootStep()])
          .append(accidentalFromAlter(chord.getRootAlter()));
        if ("major".equals(kind))
          sb.append("");
        else if ("minor".equals(kind))
          sb.append(letterM);
        else if ("augmented".equals(kind))
          sb.append(sharp).append(numberSign).append(upperNumber(5));
        else if ("diminished".equals(kind))
          sb.append("dim");
        else if ("dominant".equals(kind))
          sb.append(numberSign).append(upperNumber(7));
        else if ("suspended-second".equals(kind))
          sb.append("sus").append(numberSign).append(upperNumber(2));
        else if ("suspended-fourth".equals(kind))
          sb.append("sus").append(numberSign).append(upperNumber(4));
        else if ("major-sixth".equals(kind))
          sb.append(numberSign).append(upperNumber(6));
        else if ("major-seventh".equals(kind))
          sb.append("maj").append(numberSign).append(upperNumber(7));
        else if ("minor-seventh".equals(kind))
          sb.append("m").append(numberSign).append(upperNumber(7));
        else if ("diminished-seventh".equals(kind))
          sb.append("dim").append(numberSign).append(upperNumber(7));
        else if ("major-ninth".equals(kind))
          sb.append("maj").append(numberSign).append(upperNumber(9));
        else if ("minor-ninth".equals(kind))
          sb.append(letterM).append(numberSign).append(upperNumber(9));
        else if ("dominant-ninth".equals(kind))
          sb.append(numberSign).append(upperNumber(9));
        else if ("major-11th".equals(kind))
          sb.append("maj").append(numberSign).append(upperNumber(11));
        else if ("minor-11th".equals(kind))
          sb.append(letterM).append(numberSign).append(upperNumber(11));
        else if ("dominant-11th".equals(kind))
          sb.append(numberSign).append(upperNumber(11));
        else if ("dominant-13th".equals(kind))
          sb.append(numberSign).append(upperNumber(13));
        else LOG.warning("Unhandled harmony-chord kind '"+kind+"'");

        for (Harmony.HarmonyChord.Degree degree: chord.getAlterations())
          sb.append(accidentalFromAlter(degree.getAlter()))
            .append(numberSign).append(upperNumber(degree.getValue()));

        if (chord.hasBass())
          sb.append(slash).append(ENGLISH_STEPS[chord.getBassStep()])
            .append(accidentalFromAlter(chord.getBassAlter()));
      }
    }
    return sb.toString();
  }
  private static CharSequence accidentalFromAlter(float alter) {
    if (alter == 0) return new String();
    return new AccidentalSign(Accidental.fromAlter(alter));
  }
}
