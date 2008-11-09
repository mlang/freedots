/* -*- c-basic-offset: 2; -*- */
package org.delysid.musicxml;

import org.w3c.dom.Element;

public class Print extends Musicdata {
  public Print(Element element) { super(element); }
  public boolean isNewSystem() {
    return element.getAttribute("new-system").trim().toLowerCase()
                  .equals("yes");
  }
}
