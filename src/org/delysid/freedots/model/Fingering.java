/* -*- c-basic-offset: 2; indent-tabs-mode: nil; -*- */
/*
 * FreeDots -- MusicXML to braille music transcription
 *
 * Copyright 2008-2009 Mario Lang  All Rights Reserved.
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
package org.delysid.freedots.model;

import java.util.ArrayList;
import java.util.List;

import org.delysid.freedots.Braille;

/**
 * @see <a href="http://en.wikipedia.org/wiki/Fingering">Wikipedia:Fingering</a>
 */
public class Fingering {
  private List<Integer> fingers = new ArrayList<Integer>(1);

  public List<Integer> getFingers() { return fingers; }
  public void setFingers(List<Integer> fingers) { this.fingers = fingers; }

  public String toBrailleString() {
    StringBuilder stringBuilder = new StringBuilder();
    for (int i = 0; i < fingers.size(); i++) {
      stringBuilder.append(Braille.finger(fingers.get(i)));
      if (i < fingers.size() - 1) {
        stringBuilder.append(Braille.slur.toString());
      }
    }

    return stringBuilder.toString();
  }
}
