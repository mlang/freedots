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

/**
 * An accidental sign raises or lowers the following note from its normal
 * pitch, usually by a semitone, although microtonal music may use "fractional"
 * accidental signs, and one occasionally sees double sharps or flats, which
 * raise or lower the indicated note by a whole tone (though their most common
 * usage is in keys in which the altered note is already raised or lowered by
 * the key signature, so the altered note is only a half step from its usual
 * pitch in that key). Accidentals apply within the measure and octave in which
 * they appear, unless canceled by another accidental sign, or tied into a
 * following measure.  See {@link AccidentalContext}.
 */
public enum Accidental {
                       NATURAL(0),
              FLAT(-1),           SHARP(1),
       DOUBLE_FLAT(-2),    DOUBLE_SHARP(2),
      QUARTER_FLAT(-0.5), QUARTER_SHARP(0.5);

  private double alter;
  Accidental(final double alter) { this.alter = alter; }
  public double getAlter() { return alter; }
  public static Accidental fromAlter(double alter) {
    for (Accidental accidental: Accidental.values())
      if (accidental.getAlter() == alter) return accidental;

    return null;
  }
}
