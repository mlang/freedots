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
package freedots.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Since about 1700, accidentals have been understood to continue for the
 * remainder of the measure in which they occur, so that a subsequent note
 * on the same staff position is still affected by that accidental, unless
 * marked as an accidental on its own.  Notes on other staff positions,
 * including those an octave away, are unaffected.  Once a barline is passed,
 * the effect of the accidental ends, except when a note affected by an
 * accidental is tied to the same note across a barline.
 */
public class AccidentalContext {
  private KeySignature keySignature;
  private Map<Integer, Accidental> ranks = new HashMap<Integer, Accidental>();

  public AccidentalContext(KeySignature keySignature) {
    this.keySignature = keySignature;
  }
  public double getAlter(int octave, int step) {
    Integer rank = new Integer((octave*12) + step);
    if (ranks.containsKey(rank)) {
      return ranks.get(rank).getAlter();
    }
    return keySignature.getModifier(step);
  }
  public void setKeySignature(KeySignature keySignature) {
    this.keySignature = keySignature;
    resetToKeySignature();
  }
  public void resetToKeySignature() {
    ranks.clear();
  }
  public void accept(AbstractPitch pitch, Accidental accidental) {
    Integer rank = new Integer((pitch.getOctave()*12) + pitch.getStep());
    if (accidental != null) {
      ranks.put(rank, accidental);
    } else {
      int alter = pitch.getAlter();
      if (alter != 0) {
        if (ranks.containsKey(rank)) {
          Accidental impliedAccidental = ranks.get(rank);
          if ((int)impliedAccidental.getAlter() != alter) {
            System.err.println("Pitch alter ("+alter+") != implied accidental "+impliedAccidental);
          }
        } else {
          if (keySignature.getModifier(pitch.getStep()) != alter) {
            System.err.println("Pitch "+pitch+" != key signature alteration "+keySignature.getModifier(pitch.getStep()));
          }
        }
      }
    }
  }
}
