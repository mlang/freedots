/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.musicxml;

import org.delysid.freedots.AugmentedFraction;
import org.delysid.freedots.Fraction;

import org.delysid.freedots.model.StaffElement;
import org.delysid.freedots.model.VoiceElement;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class Note extends Musicdata implements StaffElement, VoiceElement {
  Fraction offset;

  Element grace = null;
  Pitch pitch = null;
  Text staff;
  Text voice;
  Text type;

  public Note(Fraction offset, Element element, int divisions, int durationMultiplier) {
    super(element, divisions, durationMultiplier);
    this.offset = offset;
    NodeList nodeList = element.getElementsByTagName("grace");
    if (nodeList.getLength() >= 1) {
      grace = (Element)nodeList.item(nodeList.getLength()-1);
    }
    nodeList = element.getElementsByTagName("pitch");
    if (nodeList.getLength() >= 1) {
      pitch = new Pitch((Element)nodeList.item(nodeList.getLength()-1));
    }
    staff = getTextContent(element, "staff");
    voice = getTextContent(element, "voice");
    type = getTextContent(element, "type");
  }

  public boolean isGrace() {
    if (grace != null) return true;
    return false;
  }
  public Pitch getPitch() {
    return pitch;
  }
  public String getStaffName() {
    if (staff != null) {
      return staff.getWholeText();
    }
    return null;
  }
  public String getVoiceName() {
    if (voice != null) {
      return voice.getWholeText();
    }
    return null;
  }
  public void setVoiceName(String name) {
    if (voice != null) {
      voice.replaceWholeText(name);
    }
  }

  public AugmentedFraction getAugmentedFraction() {
    if (type != null) {
      int numerator = 1;
      int denominator = 1;
      String typeString = type.getWholeText();
      if ("whole".equals(typeString)) denominator = 1;
      else if ("half".equals(typeString)) denominator = 2;
      else if ("quarter".equals(typeString)) denominator = 4;
      else if ("eighth".equals(typeString)) denominator = 8;
      else if ("16th".equals(typeString)) denominator = 16;
      else if ("32nd".equals(typeString)) denominator = 32;
      else if ("64th".equals(typeString)) denominator = 64;
      else if ("128th".equals(typeString)) denominator = 128;
      else if ("256th".equals(typeString)) denominator = 256;
      else
        System.err.println("Unhandled <type>"+typeString+"</type>");
      return new AugmentedFraction(numerator, denominator,
                                   element.getElementsByTagName("dot").getLength());
    } else {
      return new AugmentedFraction(getDuration().toInteger(divisions), divisions);
    }
  }

  public Fraction getOffset() { return offset; }

  static Text getTextContent(Element element, String childTagName) {
    NodeList nodeList = element.getElementsByTagName(childTagName);
    if (nodeList.getLength() >= 1) {
      nodeList = nodeList.item(nodeList.getLength()-1).getChildNodes();
      for (int index = 0; index < nodeList.getLength(); index++) {
        Node node = nodeList.item(index);
        if (node.getNodeType() == Node.TEXT_NODE) return (Text)node;
      }
    }
    return null;
  }
}
