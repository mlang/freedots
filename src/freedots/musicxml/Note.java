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
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import freedots.logging.Logger;
import freedots.music.Accidental;
import freedots.music.Articulation;
import freedots.music.AugmentedFraction;
import freedots.music.Clef;
import freedots.music.Event;
import freedots.music.Fermata;
import freedots.music.Fingering;
import freedots.music.Fraction;
import freedots.music.KeySignature;
import freedots.music.Ornament;
import freedots.music.Staff;
import freedots.music.RhythmicElement;
import freedots.music.Syllabic;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/** A wrapper around (the most important) note element.
 */
public final class Note implements RhythmicElement {
  private static final Logger log = Logger.getLogger(Note.class);

  static final String ACCIDENTAL_ELEMENT = "accidental";
  static final String CHORD_ELEMENT = "chord";
  private static final String LYRIC_ELEMENT = "lyric";
  private static final String NOTATIONS_ELEMENT = "notations";
  private static final String STAFF_ELEMENT = "staff";
  private static final String TIME_MODIFICATION_ELEMENT = "time-modification";

  private static final Map<String, Articulation>
  ARTICULATION_MAP = Collections.unmodifiableMap
    (new HashMap<String, Articulation>() {
      {
        put("accent", Articulation.accent);
        put("strong-accent", Articulation.strongAccent);
        put("breath-mark", Articulation.breathMark);
        put("staccato", Articulation.staccato);
        put("staccatissimo", Articulation.staccatissimo);
        put("tenuto", Articulation.tenuto);
      }
    });

  private int divisions, durationMultiplier;
  private Element element;

  private Part part;
  public Part getPart() { return part; }

  Fraction offset;
  Staff staff = null;

  Element grace = null;
  Pitch pitch = null;
  Text duration = null;

  Text staffNumber, voiceName;
  private Type type = Type.NONE;
  private final static Map<String, Type> typeMap =
    Collections.unmodifiableMap(new HashMap<String, Type>() {
      {
        put("long", Type.LONG);
        put("breve", Type.BREVE);
        put("whole", Type.WHOLE);
        put("half", Type.HALF);
        put("quarter", Type.QUARTER);
        put("eighth", Type.EIGHTH);
        put("16th", Type.SIXTEENTH);
        put("32nd", Type.THIRTYSECOND);
        put("64th", Type.SIXTYFOURTH);
        put("128th", Type.ONEHUNDREDTWENTYEIGHTH);
        put("256th", Type.TWOHUNDREDFIFTYSIXTH);
      }
    });
  private Accidental accidental = null;
  private final static Map<String, Accidental>
  accidentalMap = Collections.unmodifiableMap(new HashMap<String, Accidental>() {
      { put("natural", Accidental.NATURAL);
        put("flat", Accidental.FLAT);
        put("flat-flat", Accidental.DOUBLE_FLAT);
        put("sharp", Accidental.SHARP);
        put("sharp-sharp", Accidental.DOUBLE_SHARP);
        put("double-sharp", Accidental.DOUBLE_SHARP);
      }
    });

  private Element tie = null;
  private Element timeModification = null;

  private Lyric lyric = null;

  private Notations notations = null;

  Note(Element element, int divisions, int durationMultiplier, Part part)
    throws MusicXMLParseException
  {
    this.element = element;
    this.divisions = divisions;
    this.durationMultiplier = durationMultiplier;
    this.part = part;

    parseDOM();
  }

  void setDate(Fraction date) { offset = date; }

  private void parseDOM() {
    for (Node node = element.getFirstChild(); node != null;
         node = node.getNextSibling()) {
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        Element child = (Element)node;
        if (child.getTagName().equals("grace")) {
          grace = child;
        } else if (child.getTagName().equals("pitch")) {
          pitch = new Pitch(child);
        } else if (child.getTagName().equals("duration")) {
          duration = firstTextNode(child);
        } else if (child.getTagName().equals("tie")) {
          tie = child;
        } else if (child.getTagName().equals("voice")) {
          voiceName = firstTextNode(child);
        } else if (child.getTagName().equals("type")) {
          Text textNode = firstTextNode(child);
          if (textNode != null) {
            String typeName = textNode.getWholeText();
            String santizedTypeName = typeName.trim().toLowerCase();
            if (typeMap.containsKey(santizedTypeName))
              type = typeMap.get(santizedTypeName);
            else
              throw new MusicXMLParseException("Illegal <type> content '"+typeName+"'");
          }
        } else if (child.getTagName().equals(ACCIDENTAL_ELEMENT)) {
          if (part.getScore().encodingSupports(ACCIDENTAL_ELEMENT)) {
            Text textNode = firstTextNode(child);
            if (textNode != null) {
              String accidentalName = textNode.getWholeText();
              String santizedName = accidentalName.trim().toLowerCase();
              if (accidentalMap.containsKey(santizedName))
                accidental = accidentalMap.get(santizedName);
              else
                throw new MusicXMLParseException("Illegal <accidental>"+accidentalName+"</accidental>");
            }
          }
        } else if (child.getTagName().equals(TIME_MODIFICATION_ELEMENT)) {
          timeModification = child;
        } else if (child.getTagName().equals(STAFF_ELEMENT)) {
          staffNumber = firstTextNode(child);
        } else if (child.getTagName().equals(NOTATIONS_ELEMENT)) {
          notations = new Notations(child);
        } else if (child.getTagName().equals(LYRIC_ELEMENT)) {
          lyric = new Lyric(child);
        }
      }
    }
  }

  static Text firstTextNode(Node node) {
    for (Node child = node.getFirstChild(); node != null;
         node = node.getNextSibling()) {
      if (child.getNodeType() == Node.TEXT_NODE) return (Text)child;
    }
    return null;
  }

  public boolean isGrace() {
    return (grace != null);
  }
  public boolean isRest() {
    if ("forward".equals(element.getTagName()) ||
        element.getElementsByTagName("rest").getLength() > 0)
      return true;
    return false;
  }
  public Pitch getPitch() { return pitch; }
  public int getStaffNumber() {
    if (staffNumber != null) {
      return Integer.parseInt(staffNumber.getWholeText()) - 1;
    }
    return 0;
  }
  public String getVoiceName() {
    if (voiceName != null) {
      return voiceName.getWholeText();
    }
    return null;
  }
  public void setVoiceName(String name) {
    if (voiceName != null) {
      voiceName.replaceWholeText(name);
    }
  }

  public AugmentedFraction getAugmentedFraction() {
    if (type != Type.NONE) {
      int normalNotes = 1;
      int actualNotes = 1;
      if (timeModification != null) {
        normalNotes = Integer.parseInt(Score.getTextNode(timeModification, "normal-notes").getWholeText());
        actualNotes = Integer.parseInt(Score.getTextNode(timeModification, "actual-notes").getWholeText());
      }
      return new AugmentedFraction(type.getNumerator(), type.getDenominator(),
                                   element.getElementsByTagName("dot").getLength(),
                                   normalNotes, actualNotes);
    } else {
      return new AugmentedFraction(getDuration());
    }
  }

  public Accidental getAccidental() {
    return accidental;
  }
  public void setAccidental(Accidental accidental) {
    this.accidental = accidental;

    if (part.getScore().encodingSupports(ACCIDENTAL_ELEMENT)) {
      String accidentalName = null;
      if (accidental != null) {
        if (accidental.getAlter() == 0) accidentalName = "natural";
        else if (accidental.getAlter() == -1) accidentalName = "flat";
        else if (accidental.getAlter() == 1) accidentalName = "sharp";
      }

      Node node;
      for (node = element.getFirstChild(); node != null;
           node = node.getNextSibling()) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
          if (node.getNodeName().equals(ACCIDENTAL_ELEMENT)) {
            if (accidental != null) {
              if (accidentalName != null) node.setTextContent(accidentalName);
            } else {
              element.removeChild(node);
            }
            return;
          }
        }
      }

      for (node = element.getFirstChild(); node != null;
           node = node.getNextSibling()) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
          if (node.getNodeName().equals(TIME_MODIFICATION_ELEMENT)
           || node.getNodeName().equals("stem")
           || node.getNodeName().equals("notehead")
           || node.getNodeName().equals(STAFF_ELEMENT)
           || node.getNodeName().equals("beam")
           || node.getNodeName().equals(NOTATIONS_ELEMENT)
           || node.getNodeName().equals(LYRIC_ELEMENT)) break;
        }
      }
      Element
      newElement = element.getOwnerDocument().createElement(ACCIDENTAL_ELEMENT);
      newElement.setTextContent(accidentalName);
      element.insertBefore(newElement, node);
    }
  }

  public boolean isTieStart() {
    if (tie != null && tie.getAttribute("type").equals("start")) return true;
    return false;
  }

  public Fraction getOffset() { return offset; }
  public Staff getStaff() { return staff; }
  public void setStaff(Staff staff) { this.staff = staff; }

  private enum Type {
    LONG(4, 1), BREVE(2, 1), WHOLE(1, 1), HALF(1, 2), QUARTER(1, 4),
    EIGHTH(1, 8), SIXTEENTH(1, 16), THIRTYSECOND(1, 32),
    SIXTYFOURTH(1, 64), ONEHUNDREDTWENTYEIGHTH(1, 128),
    TWOHUNDREDFIFTYSIXTH(1, 256), NONE(0, 1);

    int numerator;
    int denominator;
    private Type(int numerator, int denominator) {
      this.numerator = numerator;
      this.denominator = denominator;
    }
    int getNumerator() { return numerator; }
    int getDenominator() { return denominator; }      
  }

  class Lyric implements freedots.music.Lyric {
    Element element;

    Lyric(Element element) {
      this.element = element;
    }
    public String getText() {
      Text textNode = Score.getTextNode(element, "text");
      if (textNode != null) return textNode.getWholeText();
      return "";
    }
    public Syllabic getSyllabic() {
      Text textNode = Score.getTextNode(element, "syllabic");
      if (textNode != null) {      
        return Enum.valueOf(Syllabic.class, textNode.getWholeText().toUpperCase());
      }
      return null;
    }
  }
  public Lyric getLyric() { return lyric; }

  public boolean equalsIgnoreOffset(Event object) {
    if (object instanceof Note) {
      Note other = (Note)object;

      if (getAugmentedFraction().equals(other.getAugmentedFraction())) {
        if (getAccidental() == other.getAccidental()) {
          if (getPitch() == null) {
            return (other.getPitch() == null);
          } else {
            return getPitch().equals(other.getPitch());
          }
        }
      }
    }
    return false;
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof Note) {
      Note other = (Note)object;

      if (getOffset().equals(other.getOffset())) {
        return equalsIgnoreOffset(other);
      }
    }
    return false;
  }

  public int getMidiChannel() {
    MidiInstrument instrument = part.getMidiInstrument(null);
    if (instrument != null) return instrument.getMidiChannel();
    return 0;
  }
  public int getMidiProgram() {
    MidiInstrument instrument = part.getMidiInstrument(null);
    if (instrument != null) return instrument.getMidiProgram();
    return 0;
  }
  public Fraction getDuration() throws MusicXMLParseException {
    if (duration != null) {
      int value = Math.round(Float.parseFloat(duration.getNodeValue()));
      Fraction fraction = new Fraction(value * durationMultiplier,
                                       4 * divisions);
      return fraction;
    }
    return getAugmentedFraction().basicFraction(); 
  }

  public Notations getNotations() { return notations; }
  private Notations createNotations() {
    Node node;
    for (node = element.getFirstChild(); node != null;
         node = node.getNextSibling()) {
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        if (node.getNodeName().equals(LYRIC_ELEMENT)) break;
      }
    }

    Element newElement = element.getOwnerDocument().createElement(NOTATIONS_ELEMENT);
    element.insertBefore(newElement, node);
    notations = new Notations(newElement);
    return notations;
  }

  private List<Slur> slurs = new ArrayList<Slur>(2);

  public List<Slur> getSlurs() { return slurs; }
  public void addSlur(Slur slur) { slurs.add(slur); }

  public Fingering getFingering() {
    if (notations != null) {
      Notations.Technical technical = notations.getTechnical();
      if (technical != null) {
        return technical.getFingering();
      }
    }

    return new Fingering();
  }
  public void setFingering(Fingering fingering) {
    if (notations == null) createNotations();
    Notations.Technical technical = notations.getTechnical();
    if (technical == null) {
      technical = notations.createTechnical();
    }
    technical.setFingering(fingering);
  }


  public Fermata getFermata() {
    if (notations != null) {
      return notations.getFermata();
    }

    return null;
  }
  public Set<Articulation> getArticulations() {
    if (notations != null) {
      return notations.getArticulations();
    }

    return EnumSet.noneOf(Articulation.class);
  }
  public Set<Ornament> getOrnaments() {
    if (notations != null) {
      return notations.getOrnaments();
    }

    return EnumSet.noneOf(Ornament.class);
  }
  public Clef getClef() {
    if (staff != null) {
      return staff.getClef(offset);
    }
    return null;
  }
  public KeySignature getActiveKeySignature() {
    if (staff != null) {
      return staff.getKeySignature(offset);
    }
    return null;
  }

  class Notations {
    private static final String TECHNICAL_ELEMENT = "technical";
    private static final String FERMATA_ELEMENT = "fermata";
    private static final String ARTICULATIONS_ELEMENT = "articulations";

    private Element element;

    private Technical technical = null;
    private Element fermata = null;
    private Set<Articulation> articulations =
      EnumSet.noneOf(Articulation.class);

    Notations(Element element) {
      this.element = element;

      for (Node node = element.getFirstChild(); node != null;
           node = node.getNextSibling()) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
          Element child = (Element)node;
          if (child.getTagName().equals(FERMATA_ELEMENT)) {
            fermata = child;
          } else if (child.getTagName().equals(TECHNICAL_ELEMENT)) {
            technical = new Technical(child);
          } else if (child.getTagName().equals(ARTICULATIONS_ELEMENT)) {
            for (Node articulationNode = child.getFirstChild();
                 articulationNode != null;
                 articulationNode = articulationNode.getNextSibling()) {
              if (articulationNode.getNodeType() == Node.ELEMENT_NODE) {
                String nodeName = articulationNode.getNodeName();
                if (ARTICULATION_MAP.containsKey(nodeName)) {
                  articulations.add(ARTICULATION_MAP.get(nodeName));
                } else {
                  log.warning("Unhandled articulation " + nodeName);
                }
              }
            }
          }
        }
      }

      if (articulations.containsAll(Articulation.mezzoStaccatoSet)) {
        articulations.removeAll(Articulation.mezzoStaccatoSet);
        articulations.add(Articulation.mezzoStaccato);
      }
    }

    Fermata getFermata() {
      if (fermata != null) {
        Fermata.Type fermataType = Fermata.Type.UPRIGHT;
        Fermata.Shape fermataShape = Fermata.Shape.NORMAL;
        if (fermata.hasAttribute("type")
         && fermata.getAttribute("type").equals("inverted"))
          fermataType = Fermata.Type.INVERTED;
        return new Fermata(fermataType, fermataShape);
      }

      return null;
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
    public Technical createTechnical() {
      Element newElement = element.getOwnerDocument()
        .createElement(TECHNICAL_ELEMENT);
      element.appendChild(newElement);
      technical = new Technical(newElement);
      return technical;
    }

    public Set<Articulation> getArticulations() { return articulations; }

    public Set<Ornament> getOrnaments() {
      Set<Ornament> ornaments = EnumSet.noneOf(Ornament.class);

      NodeList nodeList = element.getElementsByTagName("ornaments");
      if (nodeList.getLength() >= 1) {
        nodeList = ((Element)nodeList.item(nodeList.getLength()-1)).getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
          Node node = nodeList.item(i);
          if (node.getNodeType() == Node.ELEMENT_NODE) {
            if (node.getNodeName().equals("mordent")) {
              ornaments.add(Ornament.mordent);
            } else if (node.getNodeName().equals("trill-mark")) {
              ornaments.add(Ornament.trill);
            } else if (node.getNodeName().equals("turn")) {
              ornaments.add(Ornament.turn);
            } else {
              log.warning("Unhandled ornament " + node.getNodeName());
            }
          }
        }
      }

      return ornaments;
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
      private Element element;
      private Text fingering;
      Technical(Element element) {
        this.element = element;
        fingering = Score.getTextNode(element, "fingering");
      }
      public Fingering getFingering() {
        Fingering result = new Fingering();

        if (fingering != null) {
          String[] items = fingering.getWholeText().split("[ \t\n]+");
          List<Integer> fingers = new ArrayList<Integer>(items.length);
          for (String finger: items) {
            fingers.add(Integer.valueOf(finger));
          }          
          if (fingers.size() > 0) {
            result.setFingers(fingers);
          }
        }
        return result;
      }
      public void setFingering(Fingering fingering) {
        String newValue = fingering.toString(" ");
        if (this.fingering != null) {
          this.fingering.replaceWholeText(newValue);
        } else {
          Document ownerDocument = element.getOwnerDocument();
          Element newElement = ownerDocument.createElement("fingering");
          this.fingering = ownerDocument.createTextNode(newValue);
          newElement.appendChild(this.fingering);
          element.appendChild(newElement);
        }
      }
    }
  }
}
