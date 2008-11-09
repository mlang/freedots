/* -*- c-basic-offset: 2; -*- */
package org.delysid.musicxml;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Backup extends Musicdata {
  public Backup(Element element) { super(element); }
  public int getDuration() throws Exception {
    NodeList nodeList = element.getElementsByTagName("duration");
    if (nodeList.getLength() == 1) {
      Node textNode = nodeList.item(0).getChildNodes().item(0);
      int duration = Integer.parseInt(textNode.getNodeValue());
      return duration;
    }
    throw new Exception();
  }
}
