/* -*- c-basic-offset: 2; -*- */
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
 * This software is maintained by Mario Lang <mlang@delysid.org>.
 */
package org.delysid.freedots.musicxml;

import org.delysid.freedots.model.Fraction;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class Musicdata {
  int divisions;
  int durationMultiplier;
  Element element;

  public Musicdata(Element element, int divisions, int durationMultiplier) {
    this.element = element;
    this.divisions = divisions;
    this.durationMultiplier = durationMultiplier;
  }
  public Fraction getDuration() throws MusicXMLParseException {
    NodeList nodeList = element.getElementsByTagName("duration");
    if (nodeList.getLength() == 1) {
      Node textNode = nodeList.item(0).getChildNodes().item(0);
      int duration = Math.round(Float.parseFloat(textNode.getNodeValue()));
      Fraction fraction = new Fraction(duration * durationMultiplier, 4 * divisions);
      return fraction;
    }
    throw new MusicXMLParseException("Missing <duration> element");
  }
}
