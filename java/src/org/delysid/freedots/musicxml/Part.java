/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.musicxml;

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

import org.delysid.freedots.Fraction;
import org.delysid.freedots.model.MusicList;
import org.delysid.freedots.model.StartBar;
import org.delysid.freedots.model.EndBar;
import org.delysid.freedots.model.TimeSignature;
import org.delysid.freedots.model.TimeSignatureChange;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Part {
  private Element part;
  private Element scorePart;
  private MusicXML score;

  private MusicList eventList = new MusicList();

  public Part(Element part, Element scorePart, MusicXML score) throws MusicXMLParseException {
    this.part = part;
    this.scorePart = scorePart;
    this.score = score;

    int divisions = score.getDivisions();
    int durationMultiplier = 1;

    Fraction measureOffset = new Fraction(0, 1);
    TimeSignature timeSignature = new TimeSignature(4, 4);
    int staffCount = 1;

    NodeList partChildNodes = part.getChildNodes();
    for (int i = 0; i<partChildNodes.getLength(); i++) {
      Node kid = partChildNodes.item(i);
      if (kid.getNodeType() == Node.ELEMENT_NODE &&
	  "measure".equals(kid.getNodeName())) {
	Element xmlMeasure = (Element)kid;

	StartBar startBar = new StartBar(measureOffset);
	startBar.setStaffCount(staffCount);
	eventList.add(startBar);

	Chord currentChord = null;
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
	      int newStaffCount = attributes.getStaves();
	      if (newDivisions > 0) {
		durationMultiplier = divisions / newDivisions;
	      }
	      if (newStaffCount > 1 && newStaffCount != staffCount) {
		staffCount = newStaffCount;
		startBar.setStaffCount(staffCount);
	      }
	      if (newTimeSignature != null && !newTimeSignature.equals(timeSignature)) {
		timeSignature = newTimeSignature;
		eventList.add(new TimeSignatureChange(measureOffset.add(offset),
						      timeSignature));
	      }
	    } else if ("note".equals(measureChild.getNodeName())) {
	      Note note = new Note(musicdata, divisions, durationMultiplier);
	      boolean advanceTime = true;
	      boolean addNoteToEventList = true;

	      note.setOffset(measureOffset.add(offset));
	      if (currentChord != null) {
		if (elementHasChild(musicdata, "chord")) {
		  currentChord.add(note);
		  advanceTime = false;
		  addNoteToEventList = false;
		} else {
		  currentChord = null;
		}
	      }
	      if (currentChord == null && noteStartsChord(musicdata)) {
		currentChord = new Chord(note);
		currentChord.setOffset(measureOffset.add(offset));
		advanceTime = false;
		eventList.add(currentChord);
		addNoteToEventList = false;
	      }
	      if (addNoteToEventList) {
		eventList.add(note);
	      }
	      if (advanceTime) {
		offset = offset.add(note.getDuration());
	      }
	    } else if ("backup".equals(measureChild.getNodeName())) { 
	      Backup backup = new Backup(musicdata, divisions, durationMultiplier);
	      offset = offset.subtract(backup.getDuration());
	    } else if ("print".equals(measureChild.getNodeName())) {
	      Print print = new Print((Element)kid);
	      if (print.isNewSystem()) startBar.setNewSystem(true);
	    } else
	      System.err.println("Unsupported musicdata element " + measureChild.getNodeName());
	  }
	}
	measureOffset = measureOffset.add(timeSignature);

        eventList.add(new EndBar(measureOffset));
      }
    }
  }

  public MusicList getMusicList () { return eventList; }

  public String getName() {
    XPath xpath = XPathFactory.newInstance().newXPath();
    try {
      return (String) xpath.evaluate("part-name", scorePart,
				     XPathConstants.STRING);
    } catch (XPathExpressionException e) {
      return null;
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
