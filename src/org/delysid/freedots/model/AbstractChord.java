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

public abstract class
AbstractChord<E extends Event> extends ArrayList<E> implements Event {
  Fraction offset;

  AbstractChord(Fraction offset) {
    super();
    this.offset = offset;
  }
  public AbstractChord(final E initialNote) {
    this(initialNote.getOffset());
    add(initialNote);
  }
  public final Fraction getOffset() { return offset; }
  public boolean equalsIgnoreOffset(Event other) {
    if (other instanceof AbstractChord) {
      AbstractChord otherChord = (AbstractChord)other;
      if (this.size() == otherChord.size()) {
        for (int i = 0; i < size(); i++) {
          E thisE = this.get(i);
          Event otherEvent = (Event)otherChord.get(i);
          if (!thisE.equalsIgnoreOffset(otherEvent)) return false;
        }
        return true;
      }
    }
    return false;
  }
}
