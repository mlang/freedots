/* -*- c-basic-offset: 2; -*- */
package org.delysid.musicxml;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Note extends Musicdata implements StaffElement {
  int divisions;
  int durationMultiplier;
  int offset;
  public Note(Element element, int divisions, int durationMultiplier) {
    super(element);
    this.divisions = divisions;
    this.durationMultiplier = durationMultiplier;
  }
  public Pitch getPitch() {
    NodeList nodeList = element.getElementsByTagName("pitch");
    if (nodeList.getLength() == 1)
      return new Pitch((Element)nodeList.item(0));
    return null;
  }
  public int getDuration() throws Exception {
    NodeList nodeList = element.getElementsByTagName("duration");
    if (nodeList.getLength() == 1) {
      Node textNode = nodeList.item(0).getChildNodes().item(0);
      int duration = Integer.parseInt(textNode.getNodeValue());
      return duration * durationMultiplier;
    }
    throw new Exception();
  }
  public String getStaffName() {
    NodeList nodeList = element.getElementsByTagName("staff");
    if (nodeList.getLength() == 1) {
      Node textNode = nodeList.item(0).getChildNodes().item(0);
      return textNode.getNodeValue();
    }
    return null;
  }
  public int getOffset() { return offset; }
  public void setOffset(int offset) { this.offset = offset; }
}
