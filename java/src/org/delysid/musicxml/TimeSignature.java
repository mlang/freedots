/* -*- c-basic-offset: 2; -*- */
package org.delysid.musicxml;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TimeSignature {
  Element element = null;
  int numerator;
  int denominator;

  public TimeSignature(int nominator, int denominator) {
    this.numerator = numerator;
    this.denominator = denominator;
  }
  public TimeSignature(Element element) {
    this.element = element;
  }
  public int getNumerator() {
    if (element != null) {
      NodeList nodeList = element.getElementsByTagName("beats");
      if (nodeList.getLength() == 1) {
	Node textNode = nodeList.item(0).getChildNodes().item(0);
	int beats = Integer.parseInt(textNode.getNodeValue());
	return beats;
      }
    } else {
      return numerator;
    }
    return 0;
  }
  public int getDenominator() {
    if (element != null) {
      NodeList nodeList = element.getElementsByTagName("beat-type");
      if (nodeList.getLength() == 1) {
	Node textNode = nodeList.item(0).getChildNodes().item(0);
	int beatType = Integer.parseInt(textNode.getNodeValue());
	return beatType;
      }
    } else {
      return denominator;
    }
    return 0;
  }
  public boolean equals(TimeSignature other) {
    return this.getNumerator()==other.getNumerator() &&
           this.getDenominator()==other.getDenominator();
  }
}
