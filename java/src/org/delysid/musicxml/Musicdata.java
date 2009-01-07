/* -*- c-basic-offset: 2; -*- */
package org.delysid.musicxml;

import org.delysid.freedots.Fraction;

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
      int duration = Integer.parseInt(textNode.getNodeValue());
      return new Fraction(duration * durationMultiplier, 4 * divisions);
    }
    throw new MusicXMLParseException("Missing <duration> element");
  }
}
