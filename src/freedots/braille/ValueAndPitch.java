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

import freedots.music.AbstractPitch;
import freedots.music.AugmentedFraction;

public class ValueAndPitch extends Sign {
  private final AbstractPitch pitch;
  private final AugmentedFraction value;

  ValueAndPitch(final AugmentedFraction value, final AbstractPitch pitch) {
    super(getSign(value, pitch));
    this.value = value;
    this.pitch = pitch;
  }

  public String getDescription() {
    return "A " + pitch.toString() + " with duration " + value.toString();
  }
  private static String getSign(final AugmentedFraction value,
                                final AbstractPitch pitch) {
    final int log = value.getLog();
    // FIXME: breve and long notes are not handled at all
    final int valueType = log > AugmentedFraction.EIGHTH
      ? log-AugmentedFraction.SIXTEENTH : log-2;

    final int[] stepDots = { 145, 15, 124, 1245, 125, 24, 245 };
    final int[] denomDots = { 36, 3, 6, 0 };
    return String.valueOf((char)(0X2800
                                 | dotsToBits(stepDots[pitch.getStep()])
                                 | dotsToBits(denomDots[valueType])));
  }
}
