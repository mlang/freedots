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
 * This file is maintained by Mario Lang <mlang@delysid.org>.
 */
package org.delysid.freedots.musicxml;

import java.util.ArrayList;
import java.util.List;

import org.delysid.freedots.model.KeySignature;
import org.delysid.freedots.model.TimeSignature;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class Attributes {
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
      int divisions = Math.round(Float.parseFloat(textNode.getNodeValue()));
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
  public List<Clef> getClefs() {
    NodeList nodeList = element.getElementsByTagName("clef");
    List<Clef> clefs = new ArrayList<Clef>();
    for (int index = 0; index < nodeList.getLength(); index++) {
      clefs.add(new Clef((Element)nodeList.item(index)));
    }
    return clefs;
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
  public List<Key> getKeys() {
    NodeList nodeList = element.getElementsByTagName("key");
    List<Key> keys = new ArrayList<Key>();
    for (int index = 0; index < nodeList.getLength(); index++) {
      keys.add(new Key((Element)nodeList.item(index)));
    }
    return keys;
  }
  class Clef extends org.delysid.freedots.model.Clef {
    Element element = null;
    String staffName = null;
    public Clef(Element element) {
      super(getClefSignFromElement(element), getClefLineFromElement(element));
      this.element = element;
      staffName = element.getAttribute("number");
      if (staffName.equals("")) staffName = null;
    }
    public String getStaffName() { return staffName; }
  }
  class Time extends TimeSignature {
    Element element = null;
    public Time(Element element) {
      super(getBeatsFromElement(element), getBeatTypeFromElement(element));
      this.element = element;
    }
  }
  class Key extends KeySignature {
    Element element = null;
    String staffName = null;
    public Key(Element element) {
      super(getFifthsFromElement(element));
      this.element = element;
      staffName = element.getAttribute("number");
      if (staffName.equals("")) staffName = null;
    }
    public String getStaffName() { return staffName; }
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
  static Clef.Sign getClefSignFromElement(Element element) throws MusicXMLParseException {
    NodeList nodeList = element.getElementsByTagName("sign");
    int nodeCount = nodeList.getLength();
    if (nodeCount >= 1) {
      Node textNode = nodeList.item(nodeCount-1).getChildNodes().item(0);
      return Enum.valueOf(Clef.Sign.class, textNode.getNodeValue());
    }
    throw new MusicXMLParseException("missing <sign> element");
  }
  static int getClefLineFromElement(Element element) throws MusicXMLParseException {
    NodeList nodeList = element.getElementsByTagName("line");
    int nodeCount = nodeList.getLength();
    if (nodeCount >= 1) {
      Node textNode = nodeList.item(nodeCount-1).getChildNodes().item(0);

      return Integer.parseInt(textNode.getNodeValue());
    }
    throw new MusicXMLParseException("missing <line> element");
  }
}
