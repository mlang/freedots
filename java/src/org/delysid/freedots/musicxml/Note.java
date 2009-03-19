/* -*- c-basic-offset: 2; -*- */
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
 * This software is maintained by Mario Lang <mlang@delysid.org>.
 */
package org.delysid.freedots.musicxml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.delysid.freedots.model.Accidental;
import org.delysid.freedots.model.Articulation;
import org.delysid.freedots.model.AugmentedFraction;
import org.delysid.freedots.model.Clef;
import org.delysid.freedots.model.Fermata;
import org.delysid.freedots.model.Fingering;
import org.delysid.freedots.model.Fraction;
import org.delysid.freedots.model.Ornament;
import org.delysid.freedots.model.Staff;
import org.delysid.freedots.model.RhythmicElement;
import org.delysid.freedots.model.Syllabic;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public final class Note extends Musicdata implements RhythmicElement {
  Part part;
  public Part getPart() { return part; }

  Fraction offset;
  Staff staff = null;

  Element grace = null;
  Pitch pitch = null;
  Text staffNumber;
  Text voiceName;
  Type type = Type.NONE;
  private static Map<String, Type> typeMap = new HashMap<String, Type>() {
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
  };
  Accidental accidental = null;
  private static Map<String, Accidental>
  accidentalMap = new HashMap<String, Accidental>() {
    { put("natural", Accidental.NATURAL);
      put("flat", Accidental.FLAT); put("sharp", Accidental.SHARP);
    }
  };

  Element tie = null;

  Lyric lyric = null;

  Notations notations = null;

  Note(
    Fraction offset, Element element,
    int divisions, int durationMultiplier,
    Part part
  ) throws MusicXMLParseException {
    super(element, divisions, durationMultiplier);
    this.part = part;
    this.offset = offset;
    NodeList nodeList = element.getElementsByTagName("grace");
    if (nodeList.getLength() >= 1) {
      grace = (Element)nodeList.item(nodeList.getLength()-1);
    }
    nodeList = element.getElementsByTagName("pitch");
    if (nodeList.getLength() >= 1) {
      pitch = new Pitch((Element)nodeList.item(nodeList.getLength()-1));
    }
    staffNumber = Score.getTextNode(element, "staff");
    voiceName = Score.getTextNode(element, "voice");

    Text textNode = Score.getTextNode(element, "type");
    if (textNode != null) {
      String typeName = textNode.getWholeText();
      String santizedTypeName = typeName.trim().toLowerCase();
      if (typeMap.containsKey(santizedTypeName))
        type = typeMap.get(santizedTypeName);
      else
        throw new MusicXMLParseException("Illegal <type> content '"+typeName+"'");
    }
    textNode = Score.getTextNode(element, "accidental");
    if (textNode != null) {
      String accidentalName = textNode.getWholeText();
      String santizedName = accidentalName.trim().toLowerCase();
      if (accidentalMap.containsKey(santizedName))
        accidental = accidentalMap.get(santizedName);
      else
        throw new MusicXMLParseException("Illegal <accidental>"+accidentalName+"</accidental>");
    }

    nodeList = element.getElementsByTagName("tie");
    if (nodeList.getLength() >= 1) {
      tie = (Element)nodeList.item(nodeList.getLength()-1);
    }

    nodeList = element.getElementsByTagName("lyric");
    if (nodeList.getLength() >= 1) {
      lyric = new Lyric((Element)nodeList.item(nodeList.getLength()-1));
    }

    nodeList = element.getElementsByTagName("notations");
    if (nodeList.getLength() >= 1) {
      notations = new Notations((Element)nodeList.item(nodeList.getLength() - 1));
    }
  }

  public boolean isGrace() {
    if (grace != null) return true;
    return false;
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
      NodeList nodes = element.getElementsByTagName("time-modification");
      if (nodes.getLength() > 0) {
        Element timeModification = (Element)nodes.item(nodes.getLength() - 1);
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

  public Accidental getAccidental() { return accidental; }

  public boolean isTieStart() {
    if (tie != null && tie.getAttribute("type").equals("start")) return true;
    return false;
  }

  public Fraction getOffset() { return offset; }
  public Staff getStaff() { return staff; }
  public void setStaff(Staff staff) { this.staff = staff; }

  enum Type {
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

  class Lyric implements org.delysid.freedots.model.Lyric {
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

  public boolean equalsIgnoreOffset(Object object) {
    if (object instanceof Note) {
      Note other = (Note)object;

      if (getAugmentedFraction().equals(other.getAugmentedFraction())) {
        if (getAccidental() == other.getAccidental()) {
          if (getPitch() == null) {
            return other.getPitch() == null;
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
  @Override
  public Fraction getDuration() throws MusicXMLParseException {
    NodeList nodeList = element.getElementsByTagName("duration");
    if (nodeList.getLength() == 1) {
      Node textNode = nodeList.item(0).getChildNodes().item(0);
      int duration = Math.round(Float.parseFloat(textNode.getNodeValue()));
      Fraction fraction = new Fraction(duration * durationMultiplier, 4 * divisions);
      return fraction;
    }
    return getAugmentedFraction().basicFraction(); 
  }

  public Notations getNotations() { return notations; }

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

    return null;
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

    return null;
  }
  public Set<Ornament> getOrnaments() {
    if (notations != null) {
      return notations.getOrnaments();
    }

    return null;
  }
  public Clef getClef() {
    if (staff != null) {
      return staff.getClef(offset);
    }
    return null;
  }
}
