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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @see <a href="http://en.wikipedia.org/wiki/Fingering">Wikipedia:Fingering</a>
 */
public final class Fingering {
  private List<Integer> fingers = new ArrayList<Integer>(2);

  public List<Integer> getFingers() { return fingers; }
  public void setFingers(List<Integer> fingers) { this.fingers = fingers; }

  public String toString(String delimiter) {
    StringBuilder stringBuilder = new StringBuilder();
    Iterator<Integer> iter = fingers.iterator();
    while (iter.hasNext()) {
      stringBuilder.append(iter.next().toString());
      if (iter.hasNext()) stringBuilder.append(delimiter);
    }

    return stringBuilder.toString();
  }
}
