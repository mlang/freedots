package org.delysid.freedots.musicxml;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import org.delysid.freedots.model.Fingering;
import org.delysid.freedots.model.Ornament;

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

  public Set<Ornament> getOrnaments() {
    NodeList nodeList = element.getElementsByTagName("ornaments");
    if (nodeList.getLength() >= 1) {
      nodeList = ((Element)nodeList.item(nodeList.getLength()-1)).getChildNodes();
      Set<Ornament> ornaments = EnumSet.noneOf(Ornament.class);
      for (int i = 0; i < nodeList.getLength(); i++) {
        Node node = nodeList.item(i);
        if (node.getNodeType() == Node.ELEMENT_NODE) {
          if (node.getNodeName().equals("mordent")) {
            ornaments.add(Ornament.mordent);
          } else if (node.getNodeName().equals("turn")) {
            ornaments.add(Ornament.turn);
          } else {
            System.err.println("WARNING: Unhandled ornament "+node.getNodeName());
          }
        }
      }

      return ornaments;
    }

    return null;
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
