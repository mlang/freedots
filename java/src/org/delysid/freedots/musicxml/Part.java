/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.musicxml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.delysid.freedots.model.ClefChange;
import org.delysid.freedots.model.Fraction;
import org.delysid.freedots.model.GlobalClefChange;
import org.delysid.freedots.model.GlobalKeyChange;
import org.delysid.freedots.model.KeyChange;
import org.delysid.freedots.model.KeySignature;
import org.delysid.freedots.model.MusicList;
import org.delysid.freedots.model.StartBar;
import org.delysid.freedots.model.EndBar;
import org.delysid.freedots.model.TimeSignature;
import org.delysid.freedots.model.TimeSignatureChange;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class Part {
  private Element part;
  private Element scorePart;

  private Score score;
  public Score getScore() { return score; }

  private TimeSignature timeSignature = new TimeSignature(4, 4);
  private MusicList eventList = new MusicList();

  public Part(Element part, Element scorePart, Score score)
    throws MusicXMLParseException {
    this.part = part;
    this.scorePart = scorePart;
    this.score = score;

    int divisions = score.getDivisions();
    int durationMultiplier = 1;

    int measureNumber = 0;
    Fraction measureOffset = new Fraction(0, 1);
    TimeSignature lastTimeSignature = null;
    int staffCount = 1;
    EndBar endbar = null;

    List<Slur> slurs = new ArrayList<Slur>();

    NodeList partChildNodes = part.getChildNodes();
    for (int i = 0; i<partChildNodes.getLength(); i++) {
      Node kid = partChildNodes.item(i);
      if (kid.getNodeType() == Node.ELEMENT_NODE &&
	  "measure".equals(kid.getNodeName())) {
	Element xmlMeasure = (Element)kid;

	StartBar startBar = new StartBar(measureOffset, ++measureNumber);
	startBar.setStaffCount(staffCount);
	eventList.add(startBar);

        boolean repeatBackward = false;
        int endingStop = 0;

	Chord currentChord = null;
	Fraction offset = new Fraction(0, 1);
        Fraction measureDuration = new Fraction(0, 1);
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
	      if (newTimeSignature != null) {
                if (lastTimeSignature == null) {
  		  timeSignature = newTimeSignature;
                }
                lastTimeSignature = newTimeSignature;
		eventList.add(new TimeSignatureChange(measureOffset.add(offset),
						      lastTimeSignature));
                if (offset.compareTo(new Fraction(0, 1)) == 0) {
                  startBar.setTimeSignature(newTimeSignature);
                }
	      }
              List<Attributes.Clef> clefs = attributes.getClefs();
              if (clefs.size() > 0) {
                for (Attributes.Clef clef:clefs) {
                  if (clef.getStaffName() == null)
                    eventList.add(new GlobalClefChange(measureOffset.add(offset), clef));
                  else
                    eventList.add(new ClefChange(measureOffset.add(offset), clef, Integer.parseInt(clef.getStaffName()) - 1));
                }
              }
              List<Attributes.Key> keys = attributes.getKeys();
              if (keys.size() > 0) {
                for (Attributes.Key key:keys) {
                  if (key.getStaffName() == null)
                    eventList.add(new GlobalKeyChange(measureOffset.add(offset), key));
                  else
                    eventList.add(new KeyChange(measureOffset.add(offset), key, Integer.parseInt(key.getStaffName()) - 1));
                }
              }
	    } else if ("note".equals(measureChild.getNodeName())) {
	      Note note = new Note(measureOffset.add(offset),
                                   musicdata, divisions, durationMultiplier, this);
	      boolean advanceTime = !note.isGrace();
	      boolean addNoteToEventList = true;

              Notations notations = note.getNotations();
              if (notations != null) {
                for (Notations.Slur nslur:notations.getSlurs()) {
                  int number = nslur.getNumber() - 1;
                  if (nslur.getType().equals("start")) {
                    Slur slur = new Slur(note);
                    if (slurs.size() == number) {
                      slurs.add(slur);
                    } else if (slurs.size() > number) {
                      slurs.set(number, slur);
                    }
                  } else if (nslur.getType().equals("stop")) {
                    Slur slur = slurs.get(number);
                    if (slur != null) {
                      slur.add(note);
                      slurs.set(number, null);
                    }
                  }
                }
              }
              for (Slur slur:slurs) {
                if (slur != null) {
                  if (!slur.contains(note)) slur.add(note); 
                }
              }

	      if (currentChord != null) {
		if (elementHasChild(musicdata, "chord")) {
		  currentChord.add(note);
		  advanceTime = false;
		  addNoteToEventList = false;
		} else {
                  offset = offset.add(currentChord.get(0).getDuration());
		  currentChord = null;
		}
	      }
	      if (currentChord == null && noteStartsChord(musicdata)) {
		currentChord = new Chord(note);
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
            } else if ("direction".equals(measureChild.getNodeName())) {
              Direction direction = new Direction(musicdata, measureOffset.add(offset));
              eventList.add(direction);
	    } else if ("backup".equals(measureChild.getNodeName())) { 
              if (currentChord != null) {
                offset = offset.add(currentChord.get(0).getDuration());
                currentChord = null;
              }
              Backup backup = new Backup(musicdata, divisions, durationMultiplier);
              offset = offset.subtract(backup.getDuration());
            } else if ("forward".equals(measureChild.getNodeName())) {
              if (currentChord != null) {
                offset = offset.add(currentChord.get(0).getDuration());
                currentChord = null;
              }
              Note invisibleRest = new Note(measureOffset.add(offset), musicdata,
                                            divisions, durationMultiplier, this);
              eventList.add(invisibleRest);
              offset = offset.add(invisibleRest.getDuration());
	    } else if ("print".equals(measureChild.getNodeName())) {
	      Print print = new Print(musicdata);
	      if (print.isNewSystem()) startBar.setNewSystem(true);
            } else if ("sound".equals(measureChild.getNodeName())) {
              Sound sound = new Sound(musicdata, measureOffset.add(offset));
              eventList.add(sound);
            } else if ("barline".equals(measureChild.getNodeName())) {
              Barline barline = new Barline(musicdata);

              if (barline.getLocation() == Barline.Location.LEFT) {
                if (barline.getRepeat() == Barline.Repeat.FORWARD) {
                  startBar.setRepeatForward(true);
                }
                if (barline.getEnding() > 0 &&
                    barline.getEndingType() == Barline.EndingType.START) {
                  startBar.setEndingStart(barline.getEnding());
                }
              } else if (barline.getLocation() == Barline.Location.RIGHT) {
                if (barline.getRepeat() == Barline.Repeat.BACKWARD) {
                  repeatBackward = true;
                }
                if (barline.getEnding() > 0 &&
                    barline.getEndingType() == Barline.EndingType.STOP) {
                  endingStop = barline.getEnding();
                }
              }
            } else
              System.err.println("Unsupported musicdata element " + measureChild.getNodeName());
            if (offset.compareTo(measureDuration) > 0) measureDuration = offset;
	  }
	}

        if (currentChord != null) {
          offset = offset.add(currentChord.get(0).getDuration());
          if (offset.compareTo(measureDuration) > 0) measureDuration = offset;
          currentChord = null;
        }
        if (xmlMeasure.getAttribute("number").equals("0") &&
            measureDuration.compareTo(timeSignature) < 0) {
          measureOffset = measureOffset.add(measureDuration);
        } else {
          if (measureDuration.compareTo(timeSignature) != 0) {
            System.err.println("WARNING: Incomplete measure "+xmlMeasure.getAttribute("number")+": "+timeSignature+" "+measureDuration);
          }
          measureOffset = measureOffset.add(timeSignature);
        }
        if (startBar.getTimeSignature() == null) {
          startBar.setTimeSignature(lastTimeSignature);
        }

        endbar = new EndBar(measureOffset);
        if (repeatBackward) endbar.setRepeat(true);
        if (endingStop > 0) endbar.setEndingStop(endingStop);
        eventList.add(endbar);
      }
    }
    if (endbar != null) endbar.setEndOfMusic(true);
  }

  public MidiInstrument getMidiInstrument(String id) {
    NodeList nodeList = scorePart.getElementsByTagName("midi-instrument");
    for (int index = 0; index < nodeList.getLength(); index++) {
      MidiInstrument instrument = new MidiInstrument((Element)nodeList.item(index));
      if (id == null) return instrument;
      if (id.equals(instrument.getId())) return instrument;
    }

    return null;
  }

  public TimeSignature getTimeSignature() { return timeSignature; }
  public KeySignature getKeySignature() {
    for (Object event:eventList) {
      if (event instanceof GlobalKeyChange) {
        GlobalKeyChange globalKeyChange = (GlobalKeyChange)event;
        return globalKeyChange.getKeySignature();
      }
    }
    return new KeySignature(0);
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
