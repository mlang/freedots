/* -*- c-basic-offset: 2; indent-tabs-mode: nil; -*- */
/*
 * FreeDots -- MusicXML to braille music transcription
 *
 * Copyright 2008-2010 Mario Lang  All Rights Reserved.
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

import freedots.math.Fraction;
import freedots.music.Event;

public final class Direction extends AbstractDirection {
  private final List<Element> directionTypes = new ArrayList<Element>();
  private Sound sound;

  Direction(final Element element,
            final int durationMultiplier, final int divisions,
            final Fraction offset) {
    super(element, durationMultiplier, divisions, offset);

    for (Node node = element.getFirstChild(); node != null;
         node = node.getNextSibling()) {
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        Element child = (Element)node;
        if ("direction-type".equals(child.getTagName())) {
          directionTypes.add(child);
        } else if ("sound".equals(child.getTagName())) {
          sound = new Sound(child, getMoment());
        }
      }
    }
  }

  public boolean isDirective() {
    return Score.YES.equalsIgnoreCase(element.getAttribute("directive"));
  }
  public String getWords() {
    // There can be several words elements in several direction-type elements
    // TODO: Do we want to add a space when concatenating?
    StringBuilder sb = new StringBuilder();

    for (Element directionType: directionTypes) {
      for (Node node = directionType.getFirstChild(); node != null;
           node = node.getNextSibling()) {
        if (node.getNodeType() == Node.ELEMENT_NODE
         && "words".equals(node.getNodeName())) {
          Element words = (Element)node;
          sb.append(words.getTextContent());
        }
      }
    }
    if (sb.length() == 0) return null;
    return sb.toString();
  }

  private boolean isPedalType(String type) {
    for (Element directionType: directionTypes) {
      for (Node node = directionType.getFirstChild(); node != null;
           node = node.getNextSibling()) {
        if (node.getNodeType() == Node.ELEMENT_NODE
         && "pedal".equals(node.getNodeName())) {
          Element pedal = (Element)node;
          if (type.equals(pedal.getAttribute("type"))) return true;
        }
      }
    }
    return false;
  }
  public boolean isPedalPress() {
    return isPedalType("start");
  }
  public boolean isPedalRelease() {
    return isPedalType("stop");
  }

  /** Checks if this direction contains dynamics.
   * @return a list of found dynamics indicators, or null if no dynamics
   *         element was found in this direction.
   */
  public List<String> getDynamics() {
    List<String> dynamics = null;
    for (Element directionType: directionTypes) {
      for (Node node = directionType.getFirstChild(); node != null;
           node = node.getNextSibling()) {
        if (node.getNodeType() == Node.ELEMENT_NODE
         && "dynamics".equals(node.getNodeName())) {
          if (dynamics == null) dynamics = new ArrayList<String>();
          for (Node dynamicsNode = node.getFirstChild(); dynamicsNode != null;
               dynamicsNode = dynamicsNode.getNextSibling()) {
            if (dynamicsNode.getNodeType() == Node.ELEMENT_NODE) {
              Element element = (Element)dynamicsNode;
              String text;
              if ("other-dynamics".equals(element.getTagName())) {
                text = element.getTextContent();
              } else {
                text = element.getTagName();
              }
              dynamics.add(text);
            }
          }
        }
      }
    }
    return dynamics;
  }

  public Sound getSound() { return sound; }
    
  // TODO: Incomplete, needs to be rethought
  public boolean equalsIgnoreOffset(Event object) {
    if (object instanceof Direction) {
      Direction other = (Direction)object;
      Sound thisSound = this.getSound();
      Sound otherSound = other.getSound();
      if (thisSound == null || otherSound == null)
        return thisSound == otherSound;
      return thisSound.equalsIgnoreOffset(otherSound);
    }
    return false;
  }
}
