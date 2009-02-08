/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.musicxml;

import org.delysid.freedots.model.Event;
import org.delysid.freedots.model.Fraction;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Direction implements Event {
  Fraction offset;
  private Element xml;

  public Direction(Element xml, Fraction offset) {
    this.xml = xml;
    this.offset = offset;
  }

  public Sound getSound() {
    NodeList nodeList = xml.getElementsByTagName("sound");
    if (nodeList.getLength() > 0) {
      return new Sound((Element)nodeList.item(nodeList.getLength() - 1), offset);
    }
    return null;
  }
    
  public Fraction getOffset() { return offset; }
}
