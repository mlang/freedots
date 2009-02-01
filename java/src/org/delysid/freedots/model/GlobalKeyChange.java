/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.model;

public class GlobalKeyChange implements Event {
  private Fraction offset;
  private KeySignature keySignature;

  public GlobalKeyChange(Fraction offset, KeySignature keySignature) {
    this.offset = offset;
    this.keySignature = keySignature;
  }

  public KeySignature getKeySignature() { return keySignature; }

  public Fraction getOffset() { return offset; }
}
