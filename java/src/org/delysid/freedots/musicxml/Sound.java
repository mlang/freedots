/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.musicxml;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;

import org.delysid.freedots.model.Event;
import org.delysid.freedots.model.Fraction;

import org.w3c.dom.Element;

public class Sound implements Event {
  Fraction offset;
  private Element xml;

  public Sound(Element xml, Fraction offset) {
    this.xml = xml;
    this.offset = offset;
  }

  public MetaMessage getTempoMessage() {
    if (xml.hasAttribute("tempo")) {
      float tempo = Float.parseFloat(xml.getAttribute("tempo"));
      int midiTempo = Math.round(60000 / tempo * 1000);
      MetaMessage message = new MetaMessage();
      byte[] bytes = new byte[3];
      bytes[0] = (byte) (midiTempo / 0X10000);
      bytes[1] = (byte) ((midiTempo - bytes[0] * 0x10000) / 0x100);
      bytes[2] = (byte) (midiTempo - bytes[0] * 0x10000 - bytes[1] * 0x100);
      try {
        message.setMessage(0X51, bytes, bytes.length);
        return message;
      } catch (InvalidMidiDataException e) {}
    }
    return null;
  }

  public Fraction getOffset() { return offset; }
}
