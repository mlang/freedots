/* -*- c-basic-offset: 2; indent-tabs-mode: nil; -*- */
/*
 * FreeDots -- MusicXML to braille music transcription
 *
 * Copyright 2008-2009 Mario Lang  All Rights Reserved.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 3, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details (a copy is included in the LICENSE.txt file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License 
 * along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This file is maintained by Mario Lang <mlang@delysid.org>.
 */
package org.delysid.freedots.model;

public class EndBar extends VerticalEvent {
  public EndBar(Fraction offset) { super(offset); }
  boolean repeat = false;
  public boolean getRepeat() { return repeat; }
  public void setRepeat(boolean repeat) { this.repeat = repeat; }

  boolean endOfMusic = false;
  public boolean getEndOfMusic() { return endOfMusic; }
  public void setEndOfMusic(boolean endOfMusic) { this.endOfMusic = endOfMusic; }

  int endingStop = 0;
  public int getEndingStop() { return endingStop; }
  public void setEndingStop(int endingStop) { this.endingStop = endingStop; }

  public boolean equalsIgnoreOffset(Event other) {
    if (other instanceof EndBar) {
      EndBar otherBar = (EndBar)other;
      if (getRepeat() == otherBar.getRepeat() &&
	  getEndOfMusic() == otherBar.getEndOfMusic() &&
	  getEndingStop() == otherBar.getEndingStop())
	return true;
    }
    return false;
  }
}
