/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.musicxml;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Backup extends Musicdata {
  public Backup(Element element, int divisions, int durationMultiplier) {
    super(element, divisions, durationMultiplier);
  }
}
