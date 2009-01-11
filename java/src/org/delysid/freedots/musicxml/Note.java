/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.musicxml;

import org.delysid.freedots.Fraction;

import org.delysid.freedots.model.StaffElement;
import org.delysid.freedots.model.VoiceElement;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class Note extends Musicdata implements StaffElement, VoiceElement {
  Fraction offset;

  Pitch pitch = null;
  Text staff;
  Text voice;

  public Note(Fraction offset, Element element, int divisions, int durationMultiplier) {
    super(element, divisions, durationMultiplier);
    this.offset = offset;
    NodeList nodeList = element.getElementsByTagName("pitch");
    if (nodeList.getLength() >= 1) {
      pitch = new Pitch((Element)nodeList.item(nodeList.getLength()-1));
    }
    staff = getTextContent(element, "staff");
    voice = getTextContent(element, "voice");
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
