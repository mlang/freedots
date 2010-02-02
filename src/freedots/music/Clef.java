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
package freedots.music;

/** Represents a clef.
 * <p>
 * While a clef in braille music is not necessary to determine the pitch
 * of notes it is necessary to indicate clef changes to stay true to the
 * original print.
 * @see <a href="http://en.wikipedia.org/wiki/Clef">Wikipedia: Clef</a>
 */
public class Clef {
  /** The types of clefs.
   */
  public enum Sign { G, F, C, TAB, percussion, none; };

  public Sign sign;
  public int line = 0;
  public Clef(final Sign sign) {
    this.sign = sign;
  }
  public Clef(final Sign sign, final int line) {
    this(sign);
    this.line = line;
  }
  /** Returns true if this is a treble clef (G clef on line 2).
   */
  public boolean isTreble() { return sign == Sign.G && line == 2; }
  /** Returns true if this is a bass clef (F clef on line 4).
   */
  public boolean isBass() { return sign == Sign.F && line == 4; }

  public int getChordDirection() {
    switch (sign) {
    case G:
      switch (line) {
      case 2: return -1;
      }
    case F:
      switch (line) {
      case 4: return 1;
      }
    }
    return -1;
  }
}
