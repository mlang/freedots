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
package freedots.braille;

import freedots.music.Accidental;

/** Accidentals are placed immediately before the note or interval to which they
 *  belong, and must not be separated from it by anything but octave signs.
 *
 * @see <a href="http://brl.org/music/code/bmb/chap05/index.html">Accidentals
 *      and Key signatures</a>
 */
public class AccidentalSign extends Sign {
  private final Accidental accidental;

  /** Constructs a braille accidental sign.
   * @param accidental specifies the type of accidental (sharp, flat, ...)
   */
  public AccidentalSign(final Accidental accidental) {
    super(getSign(accidental));
    this.accidental = accidental;
  }

  public String getDescription() {
    return accidental.toString();
  }

  private static String getSign(final Accidental accidental) {
    switch (accidental) {
    case SHARP:        return braille(146);
    case DOUBLE_SHARP: return braille(146, 146);
    case FLAT:         return braille(126);
    case DOUBLE_FLAT:  return braille(126, 126);
    case NATURAL:      return braille(16);
    default: throw new AssertionError(accidental);
    }
  }
}
