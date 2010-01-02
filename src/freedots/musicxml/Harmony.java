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

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import freedots.Braille;
import freedots.logging.Logger;
import freedots.music.Event;
import freedots.music.Fraction;

public final class Harmony implements Event {
  private static final Logger log = Logger.getLogger(Harmony.class);

  private Element element;
  private Fraction initialDate;

  private List<HarmonyChord> chords = new ArrayList<HarmonyChord>();
  public Harmony(final Element element, final Fraction offset) {
    this.element = element;
    this.initialDate = offset;

    HarmonyChord currentChord = null;
    for (Node node = element.getFirstChild(); node != null;
         node = node.getNextSibling()) {
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        Element child = (Element)node;
        if ("root".equals(child.getTagName())
         || "function".equals(child.getTagName())) {
          chords.add(currentChord = new HarmonyChord(child));
        } else if ("kind".equals(child.getTagName())) {
          if (currentChord != null)
            currentChord.setKind(child);
        } else if ("inversion".equals(child.getTagName())) {
          if (currentChord != null)
            currentChord.setInversion(child);
        } else if ("degree".equals(child.getTagName())) {
          if (currentChord != null)
            currentChord.addDegree(child);
        }
      }
    }
    log.info("Harmony: "+Braille.toString(this));
  }
  public List<HarmonyChord> getChords() { return chords; }

  public Fraction getOffset() { return initialDate; }
  public boolean equalsIgnoreOffset(Event object) {
    return false;
  }

  @Override public String toString() {
    StringBuilder sb = new StringBuilder();
    for (HarmonyChord chord: chords) {
      sb.append(chord.toString());
    }
    return sb.toString();
  }

  public class HarmonyChord {
    private Element rootStep = null, rootAlter = null;
    private Element function = null; //Unsupported
    private Element kind;
    private Element inversion = null;

    private List<Degree> degrees = new ArrayList<Degree>();

    HarmonyChord(Element initial) {
      if ("root".equals(initial.getTagName()))
        for (Node node = initial.getFirstChild(); node != null;
             node = node.getNextSibling()) {
          if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element child = (Element)node;
            if ("root-step".equals(child.getTagName())) {
              rootStep = child;
            } else if ("root-alter".equals(child.getTagName())) {
              rootAlter = child;
            }
          }
        }
      else if ("function".equals(initial.getTagName()))
        function = initial;
    }
    public int getRootStep() {
      if (rootStep != null) {
        return "CDEFGAB".indexOf(rootStep.getTextContent().trim().toUpperCase());
      }
      log.warning("Unsupported harmony type without root-step");
      return 0;
    }
    public float getRootAlter () {
      if (rootAlter != null) {
        return Float.parseFloat(rootAlter.getTextContent());
      }
      return 0;
    }
    void setKind(Element kind) {
      this.kind = kind;
    }
    public String getKind() {
      return kind.getTextContent();
    }
    void setInversion(Element inversion) {
      this.inversion = inversion;
    }
    /** Gets a number indicating which inversion is used.
     * @return 0 for root position, 1 for first inversion, etc.
     */
    public int getInversion() {
      if (inversion != null) {
        return Integer.parseInt(inversion.getTextContent());
      }
      return 0;
    }
    void addDegree(Element degree) { degrees.add(new Degree(degree)); }
    public List<Degree> getAlterations() { return degrees; }
    @Override public String toString() {
      return (rootStep != null? rootStep.getTextContent(): function.getTextContent())
             + kind.getTextContent();
    }
    public class Degree {
      private Element value, alter, type;
      Degree(Element degree) {
        for (Node node = degree.getFirstChild(); node != null;
             node = node.getNextSibling()) {
          if (node.getNodeType() == Node.ELEMENT_NODE) {
            if ("degree-value".equals(node.getNodeName()))
              value = (Element)node;
            else if ("degree-alter".equals(node.getNodeName()))
              alter = (Element)node;
            else if ("degree-type".equals(node.getNodeName()))
              type = (Element)node;
          }
        }
      }
      public int getValue() {
        return Integer.parseInt(value.getTextContent());
      }
      public float getAlter() {
        return Float.parseFloat(alter.getTextContent());
      }
    }
  }
}
