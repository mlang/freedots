/* -*- c-basic-offset: 2; indent-tabs-mode: nil; -*- */
/*
 * FreeDots -- MusicXML to braille music transcription
 *
 * Copyright 2008-2009 Mario Lang  All Rights Reserved.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 3, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details (a copy is included in the LICENSE.txt file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License
 * along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This file is maintained by Mario Lang <mlang@delysid.org>.
 */
package freedots.musicxml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import freedots.logging.Logger;
import freedots.music.Accidental;
import freedots.music.AccidentalContext;
import freedots.music.ClefChange;
import freedots.music.Event;
import freedots.music.Fraction;
import freedots.music.GlobalKeyChange;
import freedots.music.KeyChange;
import freedots.music.KeySignature;
import freedots.music.MusicList;
import freedots.music.StartBar;
import freedots.music.EndBar;
import freedots.music.TimeSignature;
import freedots.music.TimeSignatureChange;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** A wrapper around the MusicXML part element.
 */
public final class Part {
  private static final Logger log = Logger.getLogger(Part.class);

  private Element scorePart;

  private Score score;
  public Score getScore() { return score; }

  private TimeSignature timeSignature = new TimeSignature(4, 4);
  private MusicList eventList = new MusicList();

  /** Construct a new part (and all its relevant child objects).
   *
   * @throws MusicXMLParseException if an unrecoverable error happens
   */
  public Part (
    Element part, Element scorePart,
    Score score
  ) throws MusicXMLParseException {
    this.scorePart = scorePart;
    this.score = score;

    int divisions = score.getDivisions();
    int durationMultiplier = 1;

    int measureNumber = 0;
    Fraction measureOffset = Fraction.ZERO;
    TimeSignature lastTimeSignature = null;
    int staffCount = 1;
    EndBar endbar = null;

    List<List<Slur>> slurs = new ArrayList<List<Slur>>();

    for (Element xmlElement : Score.getChildElements(part)) {
      if ("measure".equals(xmlElement.getNodeName())) {
        Element xmlMeasure = xmlElement;

        StartBar startBar = new StartBar(measureOffset, ++measureNumber);
        startBar.setStaffCount(staffCount);
        eventList.add(startBar);

        boolean repeatBackward = false;
        int endingStop = 0;

        Chord currentChord = null;
        Fraction offset = Fraction.ZERO;
        Fraction measureDuration = Fraction.ZERO;
        NodeList measureChildNodes = xmlMeasure.getChildNodes();
        for (int index = 0; index < measureChildNodes.getLength(); index++) {
          Node measureChild = measureChildNodes.item(index);
          if (measureChild.getNodeType() == Node.ELEMENT_NODE) {
            Element musicdata = (Element)measureChild;
            if ("attributes".equals(measureChild.getNodeName())) {
              Attributes attributes = new Attributes(musicdata);
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
                if (offset.compareTo(Fraction.ZERO) == 0) {
                  startBar.setTimeSignature(newTimeSignature);
                }
              }
              List<Attributes.Clef> clefs = attributes.getClefs();
              if (clefs.size() > 0) {
                for (Attributes.Clef clef:clefs) {
                  eventList.add(new ClefChange(measureOffset.add(offset),
                                               clef, clef.getStaffNumber()));
                }
              }
              List<Attributes.Key> keys = attributes.getKeys();
              if (keys.size() > 0) {
                for (Attributes.Key key: keys) {
                  if (key.getStaffName() == null)
                    eventList.add(new GlobalKeyChange(measureOffset.add(offset), key));
                  else
                    eventList.add(new KeyChange(measureOffset.add(offset), key, Integer.parseInt(key.getStaffName()) - 1));
                }
              }
            } else if ("note".equals(measureChild.getNodeName())) {
              Note note = new Note(musicdata, divisions, durationMultiplier,
                                   this);
              note.setDate(measureOffset.add(offset));
              boolean advanceTime = !note.isGrace();
              boolean addNoteToEventList = true;

              Note.Notations notations = note.getNotations();
              if (notations != null) {
                for (Note.Notations.Slur nslur:notations.getSlurs()) {
                  int number = nslur.getNumber() - 1;
                  int slurStaffNumber = note.getStaffNumber();

                  if (nslur.getType().equals("start")) {
                    Slur slur = new Slur(note);
                    while (slurStaffNumber >= slurs.size()) {
                      slurs.add(new ArrayList<Slur>());
                    }
                    while (number >= slurs.get(slurStaffNumber).size()) {
                      slurs.get(slurStaffNumber).add(slur);
                    }
                    slurs.get(slurStaffNumber).set(number, slur);
                  } else if (nslur.getType().equals("stop")) {
                    Slur slur = slurs.get(slurStaffNumber).get(number);
                    if (slur != null) {
                      slur.add(note);
                      slurs.get(slurStaffNumber).set(number, null);
                    }
                  }
                }
              }
              if (note.getStaffNumber() < slurs.size()) {
                for (Slur slur:slurs.get(note.getStaffNumber())) {
                  if (slur != null) {
                    if (!slur.contains(note)) slur.add(note); 
                  }
                }
              }

              if (currentChord != null) {
                if (elementHasChild(musicdata, Note.CHORD_ELEMENT)) {
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
              Note invisibleRest = new Note(musicdata,
                                            divisions, durationMultiplier,
                                            this);
              invisibleRest.setDate(measureOffset.add(offset));
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
              log.info("Unsupported musicdata element "
                       + measureChild.getNodeName());
            if (offset.compareTo(measureDuration) > 0) measureDuration = offset;
          }
        }

        if (currentChord != null) {
          offset = offset.add(currentChord.get(0).getDuration());
          if (offset.compareTo(measureDuration) > 0) measureDuration = offset;
          currentChord = null;
        }
        TimeSignature activeTimeSignature = lastTimeSignature != null ? lastTimeSignature : timeSignature;
        if (xmlMeasure.getAttribute("implicit").equalsIgnoreCase("yes") &&
            measureDuration.compareTo(timeSignature) < 0) {
          measureOffset = measureOffset.add(measureDuration);
        } else {
          if (measureDuration.compareTo(activeTimeSignature) != 0) {
            log.warning("Incomplete measure "
                        + xmlMeasure.getAttribute("number") + ": "
                        + timeSignature + " " + measureDuration);
          }
          measureOffset = measureOffset.add(activeTimeSignature);
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

    if (!score.encodingSupports(Note.ACCIDENTAL_ELEMENT)) {
      int staves = 1;
      KeySignature defaultKeySignature = new KeySignature(0);
      List<AccidentalContext> contexts = new ArrayList<AccidentalContext>();
      for (int i = 0; i < staves; i++) {
        contexts.add(new AccidentalContext(defaultKeySignature));
      }
      for (Event event: eventList) {
        if (event instanceof StartBar) {
          StartBar startBar = (StartBar)event;
          if (startBar.getStaffCount() != staves) {
            if (startBar.getStaffCount() > staves)
              for (int i = 0; i < (startBar.getStaffCount() - staves); i++)
                contexts.add(new AccidentalContext(defaultKeySignature));
            else if (startBar.getStaffCount() < staves)
              for (int i = 0; i < (staves - startBar.getStaffCount()); i++)
                contexts.remove(contexts.size() - 1);
            staves = startBar.getStaffCount();
          }
          for (AccidentalContext accidentalContext: contexts)
            accidentalContext.resetToKeySignature();
        } else if (event instanceof GlobalKeyChange) {
          GlobalKeyChange globalKeyChange = (GlobalKeyChange)event;
          defaultKeySignature = globalKeyChange.getKeySignature();
          for (AccidentalContext accidentalContext: contexts)
            accidentalContext.setKeySignature(defaultKeySignature);
        } else if (event instanceof KeyChange) {
          KeyChange keyChange = (KeyChange)event;
          contexts.get(keyChange.getStaffNumber())
          .setKeySignature(keyChange.getKeySignature());
        } else if (event instanceof Note) {
          calculateAccidental((Note)event, contexts);
        } else if (event instanceof Chord) {
          for (Note note: (Chord)event)
            calculateAccidental(note, contexts);
        }
      }
    }
  }

  private void calculateAccidental (
    Note note, List<AccidentalContext> contexts
  ) {
    Pitch pitch = note.getPitch();
    if (pitch != null) {
      int staffNumber = note.getStaffNumber();
      AccidentalContext accidentalContext = contexts.get(staffNumber);
      Accidental accidental = null;

      if (pitch.getAlter() != accidentalContext.getAlter(pitch.getOctave(),
                                                         pitch.getStep())) {
        accidental = Accidental.fromAlter(pitch.getAlter());
        if (accidental != null)
          note.setAccidental(accidental);
      }
      accidentalContext.accept(pitch, accidental);
    }
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
          return elementHasChild((Element)node, Note.CHORD_ELEMENT);
        } else if ("backup".equals(nodeName) || "forward".equals(nodeName))
          return false;
      }
    }
    return false;
  }
  private static boolean elementHasChild(Element element, String tagName) {
    for (Node node = element.getFirstChild(); node != null;
         node = node.getNextSibling())
      if (node.getNodeType() == Node.ELEMENT_NODE
       && node.getNodeName().equals(tagName)) return true;

    return false;
  }
}
