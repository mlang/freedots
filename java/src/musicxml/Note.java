/* -*- c-basic-offset: 2; -*- */
package musicxml;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Note extends Musicdata {
  public Note(Element element) { super(element); }
  public Pitch getPitch() {
    NodeList nodeList = element.getElementsByTagName("pitch");
    if (nodeList.getLength() == 1) {
      return new Pitch((Element)nodeList.item(0));
    }
    return null;
  }
  public int getDuration() { return 1; }
}
