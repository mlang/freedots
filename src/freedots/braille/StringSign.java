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

public class StringSign extends Sign {
  private final int stringNumber;

  public StringSign(final int stringNumber) {
    super(getSign(stringNumber));
    this.stringNumber = stringNumber;
  }
  public String getDescription() {
    return "Indicates the "+STRING_NAMES[stringNumber]+" string should be used";
  }

  private static String getSign(final int stringNumber) {
    if (stringNumber < 1) throw new IllegalArgumentException();
    if (stringNumber >= STRING_SIGNS.length) throw new IllegalArgumentException();
    return STRING_SIGNS[stringNumber - 1];
  }
  private final static String[] STRING_SIGNS =
    braille(146, new int[]{1, 12, 123, 2, 13, 23, 3});
  private final static String[] STRING_NAMES = {
    "first", "second", "third", "fourth", "fifth", "sixth", "seventh"
  };
}
