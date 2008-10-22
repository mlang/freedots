/* -*- c-basic-offset: 2; -*- */
package musicxml;

import java.util.List;
import java.util.ArrayList;

import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Element;

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
    return result;
  }
}
