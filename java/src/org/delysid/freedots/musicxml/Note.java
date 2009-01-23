/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.musicxml;

import java.util.Map;
import java.util.HashMap;

import org.delysid.freedots.model.Accidental;
import org.delysid.freedots.model.AugmentedFraction;
import org.delysid.freedots.model.Fraction;
import org.delysid.freedots.model.Staff;
import org.delysid.freedots.model.VoiceElement;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class Note extends Musicdata implements VoiceElement {
  Fraction offset;
  Staff staff = null;

  Element grace = null;
  Pitch pitch = null;
  Text staffName;
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
  private static Map<String, Accidental> accidentalMap =
    new HashMap<String, Accidental>() {
    { put("natural", Accidental.NATURAL);
      put("flat", Accidental.FLAT); put("sharp", Accidental.SHARP);
    }
  };
  Note(
    Fraction offset, Element element,
    int divisions, int durationMultiplier
  ) throws MusicXMLParseException {
    super(element, divisions, durationMultiplier);
    this.offset = offset;
    NodeList nodeList = element.getElementsByTagName("grace");
    if (nodeList.getLength() >= 1) {
      grace = (Element)nodeList.item(nodeList.getLength()-1);
    }
    nodeList = element.getElementsByTagName("pitch");
    if (nodeList.getLength() >= 1) {
      pitch = new Pitch((Element)nodeList.item(nodeList.getLength()-1));
    }
    staffName = Score.getTextContent(element, "staff");
    voiceName = Score.getTextContent(element, "voice");

    Text textNode = Score.getTextContent(element, "type");
    if (textNode != null) {
      String typeName = textNode.getWholeText();
      String santizedTypeName = typeName.trim().toLowerCase();
      if (typeMap.containsKey(santizedTypeName))
        type = typeMap.get(santizedTypeName);
      else
        throw new MusicXMLParseException("Illegal <type> content '"+typeName+"'");
    }
    textNode = Score.getTextContent(element, "accidental");
    if (textNode != null) {
      String accidentalName = textNode.getWholeText();
      String santizedName = accidentalName.trim().toLowerCase();
      if (accidentalMap.containsKey(santizedName))
        accidental = accidentalMap.get(santizedName);
      else
        throw new MusicXMLParseException("Illegal <accidental>"+accidentalName+"</accidental>");
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
  public Pitch getPitch() {
    return pitch;
  }
  public String getStaffName() {
    if (staffName != null) {
      return staffName.getWholeText();
    }
    return null;
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
      return new AugmentedFraction(type.getNumerator(), type.getDenominator(),
                                   element.getElementsByTagName("dot").getLength());
    } else {
      return new AugmentedFraction(getDuration());
    }
  }

  public Accidental getAccidental() {
    return accidental;
  }

  public Fraction getOffset() { return offset; }
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
}
