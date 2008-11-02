/* -*- c-basic-offset: 2; -*- */
package musicxml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Part {
  private Element part;
  private Element scorePart;

  public Part(Element part, Element scorePart) {
    this.part = part;
    this.scorePart = scorePart;
  }

  public String getName() {
    XPath xpath = XPathFactory.newInstance().newXPath();
    try {
      return (String) xpath.evaluate("part-name", scorePart,
				     XPathConstants.STRING);
    } catch (XPathExpressionException e) {
      return null;
    }
  }

  public List<Measure> measures() {
    List<Measure> result = new ArrayList<Measure>();
    NodeList nodes = part.getChildNodes();
    for (int i = 0; i<nodes.getLength(); i++) {
      Node kid = nodes.item(i);
      if (kid.getNodeType() == Node.ELEMENT_NODE)
        result.add(new Measure((Element)kid, this));
    }
    return result;
  }
}
