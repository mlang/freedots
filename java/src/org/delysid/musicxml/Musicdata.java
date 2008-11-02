/* -*- c-basic-offset: 2; -*- */
package org.delysid.musicxml;

import org.w3c.dom.Element;

public abstract class Musicdata {
  Element element;
  public Musicdata(Element element) { this.element = element; }
}
