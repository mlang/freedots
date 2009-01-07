/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.musicxml;


import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Pitch {
  Element element;
  public Pitch(Element element) { this.element = element; }
  public int getStep() throws Exception {
    NodeList nodeList = element.getElementsByTagName("step");
    if (nodeList.getLength() == 1) {
      Node textNode = nodeList.item(0).getChildNodes().item(0);
      return "CDEFGAB".indexOf(textNode.getNodeValue());
    }
    throw new Exception();
  }
  public int getAlter() {
    NodeList nodeList = element.getElementsByTagName("alter");
    if (nodeList.getLength() == 1) {
      Node textNode = nodeList.item(0).getChildNodes().item(0);
      return Integer.parseInt(textNode.getNodeValue());
    }
    return 0;
  }
  public int getOctave() throws Exception {
    NodeList nodeList = element.getElementsByTagName("octave");
    if (nodeList.getLength() == 1) {
      Node textNode = nodeList.item(0).getChildNodes().item(0);
      return Integer.parseInt(textNode.getNodeValue());
    }
    throw new Exception();
  }
  public int getMIDIPitch() throws Exception {
    int[] stepToChromatic = { 0, 2, 4, 5, 7, 9, 11 };
    int midiPitch = getOctave()*12 + stepToChromatic[getStep()] + getAlter();
    return midiPitch;
  }
}
