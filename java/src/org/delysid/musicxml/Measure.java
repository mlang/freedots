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

  public Measure(Element measure, Part part) {
    this.measure = measure;
    this.part = part;
  }

  public String getNumber() { return measure.getAttribute("number"); }
  public int getStaffCount() { return 1; }
  public boolean startsNewSystem() {
    for (Musicdata musicdata:musicdata()) {
      if (musicdata instanceof Print) {
        Print print = (Print)musicdata;
        if (print.isNewSystem()) return true;
      }
    }
    return false;
  }
  public List<Musicdata> musicdata() {
    List<Musicdata> result = new ArrayList<Musicdata>();
    NodeList nodes = measure.getChildNodes();
    for (int index = 0; index < nodes.getLength(); index++) {
      Node kid = nodes.item(index);
      if (kid.getNodeType() == Node.ELEMENT_NODE)
        if ("note".equals(kid.getNodeName()))
          result.add(new Note((Element)kid));
        else if ("attributes".equals(kid.getNodeName()))
          result.add(new Attributes((Element)kid));
        else if ("print".equals(kid.getNodeName()))
          result.add(new Print((Element)kid));
        else
          System.err.println("Unsupported musicdata element " + kid.getNodeName());
    }
    return result;
  }

  public List<Staff> staves() {
    List<Staff> result = new ArrayList<Staff>();
    List<Musicdata> musicdata = musicdata();
    if (musicdata.isEmpty()) return result;
    return result;
  }
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
