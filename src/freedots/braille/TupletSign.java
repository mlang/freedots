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

import freedots.music.AugmentedPowerOfTwo;
import freedots.music.Tuplet;

public class TupletSign extends Sign {
  private Tuplet tuplet;

  public TupletSign(Tuplet tuplet) {
    super(getSign(tuplet));
    this.tuplet=tuplet;
  }

  public String getDescription() {
    return "Indicates a tuplet of "+tuplet.getType();
  }

  private static String getSign(final Tuplet tuplet) {
    int type=tuplet.getType();
    final int[] tupletDots = { 23, 25, 256, 26, 235, 2356, 236, 35};
    if (type == 3 && tuplet.getParent() == null)
      return braille(23);
    return braille(456,tupletDots[type-2],3);
  }
}

