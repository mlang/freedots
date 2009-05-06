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
package org.delysid.freedots.musicxml;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;

import org.delysid.freedots.model.Event;
import org.delysid.freedots.model.Fraction;

import org.w3c.dom.Element;

public class Sound implements Event {
  Fraction offset;
  Element xml;

  public Sound(Element xml, Fraction offset) {
    this.xml = xml;
    this.offset = offset;
  }

  public MetaMessage getTempoMessage() {
    if (xml.hasAttribute("tempo")) {
      float tempo = Float.parseFloat(xml.getAttribute("tempo"));
      int midiTempo = Math.round((float)60000.0 / tempo * 1000);
      MetaMessage message = new MetaMessage();
      byte[] bytes = new byte[3];
      bytes[0] = (byte) (midiTempo / 0X10000);
      midiTempo %= 0X10000;
      bytes[1] = (byte) (midiTempo / 0X100);
      midiTempo %= 0X100;
      bytes[2] = (byte) midiTempo;
      try {
        message.setMessage(0X51, bytes, bytes.length);
        return message;
      } catch (InvalidMidiDataException e) {}
    }
    return null;
  }

  public Integer getMidiVelocity() {
    if (xml.hasAttribute("dynamics")) {
      Float dynamics = Float.parseFloat(xml.getAttribute("dynamics"));
      return new Integer(Math.round(((float)90 / (float)100) * dynamics));
    }
    return null;
  }
  public Fraction getOffset() { return offset; }
  public boolean equalsIgnoreOffset(Event object) {
    if (object instanceof Sound) {
      Sound other = (Sound)object;
      if (xml.equals(other.xml)) return true;
    }
    return false;
  }
}
