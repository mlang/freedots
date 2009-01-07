/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.model;

import org.delysid.freedots.Fraction;

public interface Event {
  public Fraction getOffset();
  public void setOffset(Fraction offset);
}

