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

import java.awt.Color;

import freedots.music.AugmentedPowerOfTwo;

/** Signifies a rest of a certain duration.
 *
 * @see <a href="http://brl.org/music/code/bmb/chap04/index.html">Chapter 4:
 *      Rests</a>
 */
public class RestSign extends Sign {
  private final AugmentedPowerOfTwo value;

  RestSign(final AugmentedPowerOfTwo value) {
    super(getSign(value));
    this.value = value;
  }

  public String getDescription() {
    return "A rest with duration " + value.toString();
  }

  private static String getSign(final AugmentedPowerOfTwo value) {
    final int power = value.getPower();
    // FIXME: breve and long notes are not handled at all
    final int log = Math.abs(power);
    final int valueType = log > Math.abs(AugmentedPowerOfTwo.QUAVER.getPower())
      ? log+AugmentedPowerOfTwo.SEMIQUAVER.getPower() : log;

    final int[] restDots = { 134, 136, 1236, 1346 };
    return braille(restDots[valueType]);
  }
  
  @Override public Color getSignColor() { return Color.cyan; }
}
