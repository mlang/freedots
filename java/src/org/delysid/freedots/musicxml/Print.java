/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.musicxml;

import org.w3c.dom.Element;

public class Print {
  Element element;

  public Print(Element element) { this.element = element; }
  public boolean isNewSystem() {
    return element.getAttribute("new-system").trim().toLowerCase()
           .equals("yes");
  }
}
