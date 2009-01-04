/* -*- c-basic-offset: 2; -*- */
package org.delysid.musicxml;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TimeSignature extends org.delysid.music.TimeSignature {
  Element element = null;
  public TimeSignature(Element element) {
    super(getBeatsFromElement(element), getBeatTypeFromElement(element));
    this.element = element;
  }
  static int getBeatsFromElement(Element element) throws MusicXMLParseException {
    NodeList nodeList = element.getElementsByTagName("beats");
    int nodeCount = nodeList.getLength();
    if (nodeCount >= 1) {
      Node textNode = nodeList.item(nodeCount-1).getChildNodes().item(0);

      return Integer.parseInt(textNode.getNodeValue());
    }
    throw new MusicXMLParseException("missing <beats> element");
  }
  static int getBeatTypeFromElement(Element element) throws MusicXMLParseException {
    NodeList nodeList = element.getElementsByTagName("beat-type");
    int nodeCount = nodeList.getLength();
    if (nodeCount >= 1) {
      Node textNode = nodeList.item(nodeCount-1).getChildNodes().item(0);

      return Integer.parseInt(textNode.getNodeValue());
    }
    throw new MusicXMLParseException("missing <beat-type> element");
  }
}
