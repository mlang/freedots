/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.model;

public class EndBar extends VerticalEvent {
  public EndBar(Fraction offset) { super(offset); }
  boolean repeat = false;
  public boolean getRepeat() { return repeat; }
  public void setRepeat(boolean repeat) { this.repeat = repeat; }

  boolean endOfMusic = false;
  public boolean getEndOfMusic() { return endOfMusic; }
  public void setEndOfMusic(boolean endOfMusic) { this.endOfMusic = endOfMusic; }
}
