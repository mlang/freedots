/* -*- c-basic-offset: 2; -*- */
package org.delysid.musicxml;

import org.delysid.Fraction;

import org.delysid.music.StaffElement;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Note extends Musicdata implements StaffElement {
  Fraction offset;

  public Note(Element element, int divisions, int durationMultiplier) {
    super(element, divisions, durationMultiplier);
  }
  public Pitch getPitch() {
    NodeList nodeList = element.getElementsByTagName("pitch");
    if (nodeList.getLength() == 1)
      return new Pitch((Element)nodeList.item(0));
    return null;
  }
  public String getStaffName() {
    NodeList nodeList = element.getElementsByTagName("staff");
    if (nodeList.getLength() == 1) {
      Node textNode = nodeList.item(0).getChildNodes().item(0);
      return textNode.getNodeValue();
    }
    return null;
  }
  public Fraction getOffset() { return offset; }
  public void setOffset(Fraction offset) { this.offset = offset; }
}
