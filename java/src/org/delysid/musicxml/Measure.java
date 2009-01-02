/* -*- c-basic-offset: 2; -*- */
package org.delysid.musicxml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

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
  public int getStaffCount() { return staves().size(); }
  public boolean startsNewSystem() {
    for (Musicdata musicdata:musicdata()) {
      if (musicdata instanceof Print) {
        Print print = (Print)musicdata;
        if (print.isNewSystem()) return true;
      }
    }
    return false;
  }
  public List<Musicdata> musicdata() {
    List<Musicdata> result = new ArrayList<Musicdata>();
    NodeList nodes = measure.getChildNodes();
    for (int index = 0; index < nodes.getLength(); index++) {
      Node kid = nodes.item(index);
      if (kid.getNodeType() == Node.ELEMENT_NODE)
        if ("note".equals(kid.getNodeName()))
          result.add(new Note((Element)kid));
        else if ("attributes".equals(kid.getNodeName()))
          result.add(new Attributes((Element)kid));
        else if ("backup".equals(kid.getNodeName()))
          result.add(new Backup((Element)kid));
        else if ("print".equals(kid.getNodeName()))
          result.add(new Print((Element)kid));
        else
          System.err.println("Unsupported musicdata element " + kid.getNodeName());
    }
    return result;
  }

  public List<Staff> staves() {
    List<Staff> staves = new ArrayList<Staff>();
    Map<String, Staff> staffMap = new HashMap<String, Staff>();
    Staff defaultStaff = null;
    int offset = 0;
    for (Musicdata musicdata:musicdata()) {
      if (musicdata instanceof Note) {
        Note note = (Note)musicdata;
        String noteStaff = note.getStaffName();
        note.setOffset(offset);
        try { offset += note.getDuration();
        } catch (Exception e) { e.printStackTrace(); }
        if (noteStaff == null) {
          if (defaultStaff == null) {
            defaultStaff = new Staff();
            staves.add(defaultStaff);
          }
          defaultStaff.add(note);
        } else {
          Staff staff = staffMap.get(noteStaff);
          if (staff == null) {
            staff = new Staff(noteStaff);
            staves.add(staff);
          }
          staff.add(note);
        }
      } else if (musicdata instanceof Backup) {
        Backup backup = (Backup)musicdata;
        try { offset -= backup.getDuration();
        } catch (Exception e) { e.printStackTrace(); }
      }
    }
    Collections.sort(staves, new StaffNameComparator());
    for (Staff staff:staves) staff.sort();
    return staves;
  }
  public Staff staves(int index) { return staves().get(index); }
  class StaffNameComparator implements Comparator<Staff> {
    public int compare(Staff s1, Staff s2) {
      return s1.getName().compareTo(s2.getName());
    }
  }
  private boolean noteStartsChord(Node note) {
    Node node = note;
    while ((node = node.getNextSibling()) != null) {
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        if ("note".equals(node.getNodeName())) {
          NodeList nodeList = ((Element)node).getElementsByTagName("chord");
          boolean hasChord = nodeList.getLength() == 1;
          if (hasChord)
            return true;
          return false;
        } else
          return false;
      }
    }
    return false;
  }
}
