/* -*- c-basic-offset: 2; -*- */
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
    step = Score.getTextContent(element, "step");
    alter = Score.getTextContent(element, "alter");
    octave = Score.getTextContent(element, "octave");
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
