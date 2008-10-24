/* -*- c-basic-offset: 2; -*- */
package musicxml;

import java.util.List;
import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Measure {
  private Part parentPart;
  private Element measure;

  public Measure(Element measure, Part part) {
    this.measure = measure;
    this.parentPart = part;
  }

  public String getNumber() {
    return measure.getAttribute("number");
  }

  public List<Element> musicData() {
    List<Element> result = new ArrayList<Element>();
    NodeList nodes = measure.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      Node kid = nodes.item(i);
      if (kid.getNodeType() == Node.ELEMENT_NODE) {
	result.add((Element)kid);
      }
    }
    return result;
  }
}
