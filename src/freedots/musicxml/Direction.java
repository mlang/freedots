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
package freedots.musicxml;

import freedots.model.Event;
import freedots.model.Fraction;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public final class Direction implements Event {
  Fraction offset;
  private Element xml;

  public Direction(Element xml, Fraction offset) {
    this.xml = xml;
    this.offset = offset;
  }

  public Sound getSound() {
    NodeList nodeList = xml.getElementsByTagName("sound");
    if (nodeList.getLength() > 0) {
      return new Sound((Element)nodeList.item(nodeList.getLength() - 1), offset);
    }
    return null;
  }
    
  public Fraction getOffset() { return offset; }
  public boolean equalsIgnoreOffset(Event object) {
    if (object instanceof Direction) {
      Direction other = (Direction)object;
      Sound thisSound = this.getSound();
      Sound otherSound = other.getSound();
      if (thisSound == null || otherSound == null)
        return thisSound == otherSound;
      return thisSound.equalsIgnoreOffset(otherSound);
    }
    return false;
  }
}
