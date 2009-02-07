/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.musicxml;

import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class MidiInstrument {
  private Element xml;

  private Text midiProgram;
  private Text midiChannel;

  public MidiInstrument(Element xml) {
    this.xml = xml;
    midiChannel = Score.getTextNode(xml, "midi-channel");
    midiProgram = Score.getTextNode(xml, "midi-program");
  }

  public String getId() { return xml.getAttribute("id"); }

  public int getMidiProgram() {
    if (midiProgram != null)
      return Integer.parseInt(midiProgram.getWholeText()) - 1;
    return 0;
  }

  public int getMidiChannel() {
    if (midiChannel != null)
      return Integer.parseInt(midiChannel.getWholeText()) - 1;
    return 0;
  }
}

