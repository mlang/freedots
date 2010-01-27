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

public class RepeatASign extends BrailleList {
  private final Sign sign;
  private final int repetitions;

  public RepeatASign(final Sign sign, final int repetitions) {
    super();
    this.sign = sign;
    this.repetitions = repetitions;

    if (repetitions > 3) {
      add(new UpperNumber(repetitions));
      add(sign);
    } else if (repetitions == 3) {
      add(sign);
      add((Sign)sign.clone());
      add((Sign)sign.clone());
    } else if (repetitions == 2) {
      add(sign);
      add((Sign)sign.clone());
    } else if (repetitions == 1) {
      add(sign);
    }
  }
}
