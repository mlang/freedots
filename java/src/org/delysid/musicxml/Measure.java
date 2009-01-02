/* -*- c-basic-offset: 2; -*- */
package org.delysid.musicxml;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Measure {
  Part part;
  Element measure;
  List<Staff> staves = new ArrayList<Staff>();

  public Measure(Element measure, Part part) {
    this.measure = measure;
    this.part = part;
  }

  public String getNumber() { return measure.getAttribute("number"); }
  public void addStaff(Staff staff) { staves.add(staff); }
  public int getStaffCount() { return staves.size(); }
  public boolean startsNewSystem() {
    NodeList nodes = measure.getChildNodes();
    for (int index = 0; index < nodes.getLength(); index++) {
      Node kid = nodes.item(index);
      if (kid.getNodeType() == Node.ELEMENT_NODE &&
	  "print".equals(kid.getNodeName())) {
	Print print = new Print((Element)kid);
	if (print.isNewSystem()) return true;
      }
    }
    return false;
  }
  public List<Staff> getStaves() { return staves; }
  public Staff staves(int index) { return staves.get(index); }
  private boolean noteStartsChord(Node note) {
    Node node = note;
    while ((node = node.getNextSibling()) != null) {
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        if ("note".equals(node.getNodeName())) {
          NodeList nodeList = ((Element)node).getElementsByTagName("chord");
          boolean hasChord = nodeList.getLength() == 1;
          if (hasChord)
            return true;
          return false;
        } else
          return false;
      }
    }
    return false;
  }
}
