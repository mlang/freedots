package org.delysid.freedots.musicxml;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

class Notations {
  private Element element;
  Notations(Element element) { this.element = element; }

  public List<Slur> getSlurs() {
    NodeList nodeList = element.getElementsByTagName("slur");
    List<Slur> slurs = new ArrayList<Slur>();
    for (int i = 0; i < nodeList.getLength(); i++) {
      slurs.add(new Slur((Element)nodeList.item(i)));
    }
    return slurs;
  }    

  class Slur {
    Element element;
    Slur(Element element) { this.element = element; }
    public int getNumber() {
      if (element.hasAttribute("number"))
        return Integer.parseInt(element.getAttribute("number"));
      return 1;
    }
    public String getType() { return element.getAttribute("type"); }
  }
}
