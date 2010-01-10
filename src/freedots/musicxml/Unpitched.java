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

/** A wrapper around the unpitched element (a child of {@link Note}).
 */
public final class Unpitched extends freedots.music.AbstractPitch {
  private Element element;
  private Element step, octave;
  Unpitched(final Element element) {
    this.element = element;

    for (Node node = element.getFirstChild(); node != null;
         node = node.getNextSibling()) {
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        Element child = (Element)node;
        if ("display-step".equals(child.getTagName())) {
          step = child;
        } else if ("display-octave".equals(child.getTagName())) {
          octave = child;
        }
      }
    }
  }
  public int getStep() {
    if (step != null) {
      return Pitch.convertStep(step.getTextContent());
    }
    // TODO
    return 0;
  }
  public int getAlter() { return 0; }
  public int getOctave() {
    if (octave != null) {
      return Integer.parseInt(octave.getTextContent());
    }
    // TODO
    return 4;
  }
}
