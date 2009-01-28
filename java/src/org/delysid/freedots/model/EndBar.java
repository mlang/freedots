/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.model;

public class EndBar extends VerticalEvent {
  public EndBar(Fraction offset) { super(offset); }
  boolean repeat = false;
  public boolean getRepeat() { return repeat; }
  public void setRepeat(boolean repeat) { this.repeat = repeat; }
}
