/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.model;

public class GlobalClefChange implements Event {
  private Fraction offset;
  private Clef clef;

  public GlobalClefChange(Fraction offset, Clef clef) {
    this.offset = offset;
    this.clef = clef;
  }

  public Clef getClef() { return clef; }

  public Fraction getOffset() { return offset; }
}
