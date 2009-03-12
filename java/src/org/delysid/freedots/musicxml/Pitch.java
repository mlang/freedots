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

import org.w3c.dom.Element;
import org.w3c.dom.Text;

public final class Pitch extends org.delysid.freedots.model.AbstractPitch {
  Element element;
  Text step = null;
  Text alter = null;
  Text octave = null;

  public Pitch(Element element) throws MusicXMLParseException {
    this.element = element;
    step = Score.getTextNode(element, "step");
    alter = Score.getTextNode(element, "alter");
    octave = Score.getTextNode(element, "octave");
    if (step == null || octave == null) {
      throw new MusicXMLParseException("Missing step or octave element");
    }
  }
  public int getStep() {
    return "CDEFGAB".indexOf(step.getWholeText().trim().toUpperCase());
  }
  public int getAlter() {
    return alter != null? Integer.parseInt(alter.getWholeText()): 0;
  }
  public int getOctave() {
    return Integer.parseInt(octave.getWholeText());
  }
}
