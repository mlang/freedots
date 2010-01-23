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

public class OctaveSign extends Atom {
  private final int octave;
  public OctaveSign(final int octave) {
    super(getBraille(octave));
    this.octave = octave;
  }
  public String getDescription () {
    return new StringBuilder()
      .append("Indicates that the following note belongs to the ")
      .append(OCTAVE_NAMES[octave]).append(" octave.").toString();
  } 

  private static String getBraille(final int octave) {
    return OCTAVE_SIGNS[octave];
  }
  private static final String[] OCTAVE_SIGNS = new String[] {
    braille(4, 4), braille(4), braille(45), braille(456), braille(5),
    braille(46), braille(56), braille(6), braille(6, 6)
  };
  private static final String[] OCTAVE_NAMES = new String[] {
    "subsubcontra", "sub-contra", "contra", "great", "small",
    "one-lined", "two-lined", "three-lined", "four-lined",
    "five-lined", "six-lined"
  };
}
