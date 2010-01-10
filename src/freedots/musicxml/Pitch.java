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
import org.w3c.dom.Text;

/** A wrapper around the pitch element (a child of {@link Note}).
 */
public final class Pitch extends freedots.music.AbstractPitch {
  private Element element;
  private Text step, alter, octave;

  Pitch(Element element) throws MusicXMLParseException {
    this.element = element;
    step = Score.getTextNode(element, "step");
    alter = Score.getTextNode(element, "alter");
    octave = Score.getTextNode(element, "octave");
    if (step == null || octave == null) {
      throw new MusicXMLParseException("Missing step or octave element");
    }
  }
  public int getStep() {
    return convertStep(step.getWholeText());
  }
  public int getAlter() {
    return alter != null? Integer.parseInt(alter.getWholeText()): 0;
  }
  public int getOctave() {
    return Integer.parseInt(octave.getWholeText());
  }

  public static int convertStep(String step) {
    return "CDEFGAB".indexOf(step.trim().toUpperCase());
  }
}
