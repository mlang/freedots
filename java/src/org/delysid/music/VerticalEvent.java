/* -*- c-basic-offset: 2; -*- */
package org.delysid.music;

import org.delysid.Fraction;

public abstract class VerticalEvent implements Event {
  Fraction offset;

  public VerticalEvent(Fraction offset) { this.offset = offset; }

  public Fraction getOffset() { return offset; }
  public void setOffset(Fraction offset) { this.offset = offset; }
}
