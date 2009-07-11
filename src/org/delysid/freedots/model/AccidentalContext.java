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

import java.util.HashMap;
import java.util.Map;

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
    resetToSignature();
  }
  public void resetToSignature() {
    ranks.clear();
  }
}
