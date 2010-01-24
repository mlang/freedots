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

import freedots.musicxml.Direction;

public class BrailleWords extends BrailleList {
  private final String text;
  private final Direction scoreObject;

  public BrailleWords(String text, Direction direction) {
    super();
    this.text = text;
    this.scoreObject = direction;

    add(new WordSign());
    add(new Text(text));
  }
  @Override public String getDescription() {
    return "A direction that says: " + text;
  }
  @Override public boolean needsGuideDot(final BrailleSequence next) {
    if (next.length() > 0) {
      final char ch = next.charAt(0);
      if (((int)ch & 0X2807) > 0X2800) return true;
    }

    return false;
  }
  @Override public Object getScoreObject() { return scoreObject; }
}
