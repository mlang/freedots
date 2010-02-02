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
package freedots.musicxml;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import freedots.math.Fraction;

/** A wrapper around the backup element.
 */
final class Backup {
  private final int divisions, durationMultiplier;
  private final Element element;

  Backup(final Element element,
         final int divisions, final int durationMultiplier) {
    this.element = element;
    this.divisions = divisions;
    this.durationMultiplier = durationMultiplier;
  }
  public Fraction getDuration() throws MusicXMLParseException {
    NodeList nodeList = element.getElementsByTagName("duration");
    if (nodeList.getLength() == 1) {
      Node textNode = nodeList.item(0).getChildNodes().item(0);
      int duration = Math.round(Float.parseFloat(textNode.getNodeValue()));
      return new Fraction(duration * durationMultiplier, 4 * divisions);
    }
    throw new MusicXMLParseException("Missing <duration> element");
  }
}
