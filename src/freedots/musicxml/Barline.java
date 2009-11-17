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

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Barline {
  Element element;

  Element ending = null;
  public Barline(Element element) {
    this.element = element;
    
    NodeList nodeList = element.getElementsByTagName("ending");
    if (nodeList.getLength() >= 1)
      ending = (Element)nodeList.item(nodeList.getLength()-1);
  }
  public Location getLocation() {
    String attrValue = element.getAttribute("location").toUpperCase();
    if (attrValue.length() > 0)
      return Enum.valueOf(Location.class, attrValue);
    return Location.RIGHT;
  }
  public Repeat getRepeat() {
    NodeList nodeList = element.getElementsByTagName("repeat");
    if (nodeList.getLength() >= 1) {
      Element repeat = (Element)nodeList.item(nodeList.getLength()-1);
      return Enum.valueOf(Repeat.class, repeat.getAttribute("direction").toUpperCase());
    }
    return null;
  }
  public int getEnding() {
    if (ending != null && ending.getAttribute("number").length() > 0) {
      return Integer.parseInt(ending.getAttribute("number"));
    }
    return 0;
  }
  public EndingType getEndingType() {
    if (ending != null) {
      return Enum.valueOf(EndingType.class, ending.getAttribute("type").toUpperCase());
    }
    return null;
  }
  public enum Location { LEFT, RIGHT, MIDDLE; }
  public enum Repeat { BACKWARD, FORWARD; }
  public enum EndingType { START, STOP, DISCONTINUE; }
}
