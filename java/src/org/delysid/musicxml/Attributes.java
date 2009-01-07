/* -*- c-basic-offset: 2; -*- */
package org.delysid.musicxml;

import org.delysid.music.KeySignature;
import org.delysid.music.TimeSignature;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Attributes {
  Element element;
  int divisions;
  public Attributes(Element element, int divisions) {
    this.element = element;
    this.divisions = divisions;
  }
  public int getDivisions() {
    NodeList nodeList = element.getElementsByTagName("divisions");
    if (nodeList.getLength() == 1) {
      Node textNode = nodeList.item(0).getChildNodes().item(0);
      int divisions = Integer.parseInt(textNode.getNodeValue());
      return divisions;
    }
    return 0;
  }
  public int getStaves() {
    NodeList nodeList = element.getElementsByTagName("staves");
    if (nodeList.getLength() == 1) {
      Node textNode = nodeList.item(0).getChildNodes().item(0);
      int staves = Integer.parseInt(textNode.getNodeValue());
      return staves;
    }
    return 0;
  }
  public Time getTime() {
    NodeList nodeList = element.getElementsByTagName("time");
    if (nodeList.getLength() == 1) {
      return new Time((Element)nodeList.item(0));
    } else if (nodeList.getLength() > 1) {
      System.err.println("Unhandled multiple time signatures in attributes");
    }
    return null;
  }
  public Key getKey() {
    NodeList nodeList = element.getElementsByTagName("key");
    if (nodeList.getLength() == 1) {
      return new Key((Element)nodeList.item(0));
    } else if (nodeList.getLength() > 1) {
      System.err.println("Unhandled multiple key signatures in <attributes>");
    }
    return null;
  }
  public class Time extends TimeSignature {
    Element element = null;
    public Time(Element element) {
      super(getBeatsFromElement(element), getBeatTypeFromElement(element));
      this.element = element;
    }
  }
  public class Key extends KeySignature {
    Element element = null;
    public Key(Element element) {
      super(getFifthsFromElement(element));
      this.element = element;
    }
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
  static int getFifthsFromElement(Element element) throws MusicXMLParseException {
    NodeList nodeList = element.getElementsByTagName("fifths");
    int nodeCount = nodeList.getLength();
    if (nodeCount >= 1) {
      Node textNode = nodeList.item(nodeCount-1).getChildNodes().item(0);
     
      return Integer.parseInt(textNode.getNodeValue());
    }
    throw new MusicXMLParseException("missing <fifths> element");
  }
}
