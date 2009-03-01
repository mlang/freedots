package org.delysid.freedots.musicxml;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import org.delysid.freedots.model.Fingering;

class Notations {
  private Element element;
  private Technical technical = null;

  Notations(Element element) {
    this.element = element;

    NodeList nodeList = element.getElementsByTagName("technical");
    if (nodeList.getLength() >= 1) {
      technical = new Technical((Element)nodeList.item(nodeList.getLength() - 1));
    }
  }

  public List<Slur> getSlurs() {
    NodeList nodeList = element.getElementsByTagName("slur");
    List<Slur> slurs = new ArrayList<Slur>();
    for (int i = 0; i < nodeList.getLength(); i++) {
      slurs.add(new Slur((Element)nodeList.item(i)));
    }
    return slurs;
  }    

  public Technical getTechnical() { return technical; }

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
  class Technical {
    Element element;
    Technical(Element element) { this.element = element; }
    public Fingering getFingering() {
      Text text = Score.getTextNode(element, "fingering");
      if (text != null) {
        String[] items = text.getWholeText().split("[ \t\n]+");
        List<Integer> fingers = new ArrayList<Integer>(2);
        for (int i = 0; i < items.length; i++) {
          fingers.add(new Integer(Integer.parseInt(items[i])));
        }          
        if (fingers.size() > 0) {
          Fingering fingering = new Fingering();
          fingering.setFingers(fingers);
          return fingering;
        }
      }
      return null;
    }
  }
}
