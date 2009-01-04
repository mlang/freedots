/* -*- c-basic-offset: 2; -*- */
package org.delysid.musicxml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.delysid.Fraction;
import org.delysid.music.TimeSignature;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Part {
  private Element part;
  private Element scorePart;
  private MusicXML score;

  public Part(Element part, Element scorePart, MusicXML score) {
    this.part = part;
    this.scorePart = scorePart;
    this.score = score;
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
    int divisions = score.getDivisions();
    int durationMultiplier = 1;

    Fraction measureOffset = new Fraction(0, 1);
    TimeSignature timeSignature = new TimeSignature(4, 4);

    NodeList partChildNodes = part.getChildNodes();
    for (int i = 0; i<partChildNodes.getLength(); i++) {
      Node kid = partChildNodes.item(i);
      if (kid.getNodeType() == Node.ELEMENT_NODE &&
	  "measure".equals(kid.getNodeName())) {
	Element xmlMeasure = (Element)kid;
	Measure measure = new Measure(xmlMeasure, this);
	Chord currentChord = null;
	List<Staff> staves = new ArrayList<Staff>();
	Map<String, Staff> staffMap = new HashMap<String, Staff>();
	Staff defaultStaff = null;
	Fraction offset = new Fraction(0, 1);
	NodeList measureChildNodes = xmlMeasure.getChildNodes();
	for (int index = 0; index < measureChildNodes.getLength(); index++) {
	  Node measureChild = measureChildNodes.item(index);
	  if (measureChild.getNodeType() == Node.ELEMENT_NODE) {
	    Element musicdata = (Element)measureChild;

	    if ("attributes".equals(measureChild.getNodeName())) {
	      Attributes attributes = new Attributes(musicdata, divisions);
	      int newDivisions = attributes.getDivisions();
	      Attributes.Time newTimeSignature = attributes.getTime();
	      if (newDivisions > 0) {
		durationMultiplier = divisions / newDivisions;
	      }
	      if (newTimeSignature != null && !newTimeSignature.equals(timeSignature)) {
		timeSignature = newTimeSignature;
	      }
	    } else if ("note".equals(measureChild.getNodeName())) {
	      Note note = new Note(musicdata, divisions, durationMultiplier);
	      boolean advanceTime = true;
	      String noteStaffName = note.getStaffName();
	      
	      note.setOffset(measureOffset.add(offset));
	      if (currentChord != null) {
		if (elementHasChild(musicdata, "chord")) {
		  currentChord.add(note);
		  advanceTime = false;
		} else {
		  currentChord = null;
		}
	      }
	      if (currentChord == null && noteStartsChord(musicdata)) {
		currentChord = new Chord(note);
		currentChord.setOffset(measureOffset.add(offset));
		advanceTime = false;
	      }
	      if (advanceTime) {
		try { offset = offset.add(note.getDuration());
		} catch (Exception e) { e.printStackTrace(); }
	      }
	      if (noteStaffName == null) {
		if (defaultStaff == null) {
		  defaultStaff = new Staff();
		  measure.addStaff(defaultStaff);
		}
		defaultStaff.add(note);
	      } else {
		Staff staff = staffMap.get(noteStaffName);
		if (staff == null) {
		  staff = new Staff(noteStaffName);
		  measure.addStaff(staff);
		}
		staff.add(note);
	      }
	    } else if ("backup".equals(measureChild.getNodeName())) { 
	      Backup backup = new Backup(musicdata, divisions, durationMultiplier);
	      try { offset = offset.subtract(backup.getDuration());
	      } catch (Exception e) { e.printStackTrace(); }
	    } else if ("print".equals(measureChild.getNodeName())) {
	    } else
	      System.err.println("Unsupported musicdata element " + measureChild.getNodeName());
	  }
	}
	Collections.sort(staves, new StaffNameComparator());
	for (Staff staff:staves) staff.sort();
        result.add(measure);

	measureOffset = measureOffset.add(timeSignature);
      }
    }
    return result;
  }
  class StaffNameComparator implements Comparator<Staff> {
    public int compare(Staff s1, Staff s2) {
      return s1.getName().compareTo(s2.getName());
    }
  }
  private static boolean noteStartsChord(Node note) {
    Node node = note;
    while ((node = node.getNextSibling()) != null) {
      if (node.getNodeType() == Node.ELEMENT_NODE) {
	String nodeName = node.getNodeName();
        if ("note".equals(nodeName)) {
          return elementHasChild((Element)node, "chord");
        } else if ("backup".equals(nodeName) || "forward".equals(nodeName))
          return false;
      }
    }
    return false;
  }
  private static boolean elementHasChild(Element element, String tagName) {
    NodeList nodeList = element.getElementsByTagName("chord");
    return nodeList.getLength() >= 1;
  }
}
