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
import freedots.music.KeySignature;

/** Braille representation of a key signature.
 */
public class BrailleKeySignature extends RepeatASign {
  private final KeySignature keySignature;

  public BrailleKeySignature(final KeySignature keySignature) {
    super(createSign(keySignature), Math.abs(keySignature.getType()));
    this.keySignature = keySignature;
  }

  private static Sign createSign(final KeySignature keySignature) {
    if (keySignature.getType() > 0)
      return new AccidentalSign(Accidental.SHARP);
    else if (keySignature.getType() < 0)
      return new AccidentalSign(Accidental.FLAT);
    else return null;
  }
}
