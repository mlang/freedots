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
package freedots.music;

@SuppressWarnings("serial")
public class Voice extends MusicList {
  private String name;

  Voice(final String name) {
    super();
    this.name = name;
  }

  public int countEqualsAtBeginning(Voice other) {
    int elementCount = Math.min(this.size(), other.size());
    int index = 0;
    for (index = 0; index < elementCount; index++) {
      if (!this.get(index).equals(other.get(index))) break;
    }
    return index;
  }
  public int countEqualsAtEnd(Voice other) {
    int count = 0;

    // FIXME: Implement it
    return count;
  }
  public boolean restsOnly() {
    for (Event event:this)
      if (event instanceof StaffElement)
        if (!((StaffElement)event).isRest()) return false;
    return true;
  }
  public int averagePitch() {
    double value = 0;
    int count = 0;
    for (Event event : this) {
      if (event instanceof RhythmicElement) {
        RhythmicElement rhythmicElement = (RhythmicElement) event;
        AbstractPitch pitch = rhythmicElement.getPitch();
        if (pitch != null) {
          value += pitch.getMIDIPitch();
          count += 1;
        }
      }
    }
    if (count == 0) return 0;
    return (int)Math.round(value / count);
  }
  public void swapPosition(Voice other) {
    String oldName = this.name;
    String newName = other.name;
    for (Event event:other) ((VoiceElement)event).setVoiceName(this.name);
    for (Event event:this) ((VoiceElement)event).setVoiceName(other.name);
    this.name = newName;
    other.name = oldName;
  }
}
