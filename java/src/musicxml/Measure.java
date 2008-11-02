/* -*- c-basic-offset: 2; -*- */
package musicxml;

import java.util.List;
import java.util.ArrayList;

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

  public List<Musicdata> musicdata() {
    List<Musicdata> result = new ArrayList<Musicdata>();
    NodeList nodes = measure.getChildNodes();
    for (int index = 0; index < nodes.getLength(); index++) {
      Node kid = nodes.item(index);
      if (kid.getNodeType() == Node.ELEMENT_NODE) {
	if ("note".equals(kid.getNodeName())) {
	  result.add(new Note((Element)kid));
	} else if ("attributes".equals(kid.getNodeName())) {
	  result.add(new Attributes((Element)kid));
	} else {
	  System.err.println("Unsupported musicdata element " + kid.getNodeName());
	}
      }
    }
    return result;
  }
}
