/* -*- c-basic-offset: 2; -*- */
package org.delysid.music;

import org.delysid.Fraction;

public interface Event {
  public Fraction getOffset();
  public void setOffset(Fraction offset);
}

