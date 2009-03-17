/* -*- c-basic-offset: 2; -*- */
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
 * This software is maintained by Mario Lang <mlang@delysid.org>.
 */
package org.delysid.freedots.model;

import org.delysid.freedots.Braille;

/**
 * Articulation refers to the direction or performance technique which affects the
 * transition or continuity on single note or between multiple notes or sounds.
 *
 * There are many different kinds of articulation, each having a different effect
 * on how the note is played.  Each articulation is represented by a different
 * symbol placed above or below the note (depending on its position on the staff).
 *
 * @see <a href="http://en.wikipedia.org/wiki/Articulation_(music)">Wikipedia:Articulation_(music)</a>
 */
public enum Articulation {
  accent, breathMark,
  staccato, staccatissimo, tenuto;

  public Braille toBraille() {
    switch (this) {
      case accent: return Braille.accent;
      case breathMark: return Braille.breathMark;
      case staccato: return Braille.staccato;
      case staccatissimo: return Braille.staccatissimo;
      case tenuto: return Braille.tenuto;
    }
    return null;
  }
}
