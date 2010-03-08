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

import java.util.Iterator;

import freedots.music.Fingering;

/** The braille representation of a fingering indicator.
 *
 * @see <a href="http://brl.org/music/code/bmb/chap14/index.html">Chapter 14:
 *      Fingering</a>
 */
public class BrailleFingering extends BrailleList {
  private final Fingering fingering;

  public BrailleFingering(final Fingering fingering) {
    super();
    this.fingering = fingering;

    Iterator<Integer> iter = fingering.getFingers().iterator();
    while (iter.hasNext()) {
      add(new Finger(iter.next()));
      if (iter.hasNext())
        add(new SlurSign() {
              @Override public String getDescription() {
                return "Indicates a silent finger change";
              }
            });
    }
  }

  @Override public String getDescription() {
    return "Fingering: " + fingering.toString();
  }

  public static class Finger extends Sign {
    private final int finger;

    Finger(final int finger) {
      super(getSign(finger));
      this.finger = finger;
    }
    public String getDescription() {
      return "Indicates that finger " + finger + " should be used";
    }
    private static String getSign(final int number) {
      return FINGERS[number - 1];
    }
    private static final String[] FINGERS = new String[] {
      braille(1), braille(12), braille(123), braille(2), braille(13)
    };
  }
}

