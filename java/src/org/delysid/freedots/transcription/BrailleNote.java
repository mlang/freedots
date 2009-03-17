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
package org.delysid.freedots.transcription;

import java.util.Set;

import org.delysid.freedots.Braille;
import org.delysid.freedots.Options;
import org.delysid.freedots.model.AbstractPitch;
import org.delysid.freedots.model.Accidental;
import org.delysid.freedots.model.Articulation;
import org.delysid.freedots.model.Fermata;
import org.delysid.freedots.model.Fingering;
import org.delysid.freedots.model.Ornament;
import org.delysid.freedots.model.Slur;
import org.delysid.freedots.musicxml.Note;

class BrailleNote extends BrailleString {
  private AbstractPitch lastPitch;
  private Options options;

  BrailleNote(Note note, AbstractPitch lastPitch) {
    super(null, note);
    this.lastPitch = lastPitch;
    this.options = Options.getInstance();
  }

  AbstractPitch getPitch() {
    Note note = (Note)model;
    return note.getPitch();
  }

  @Override
  public String toString() {
    String braille = "";
    Note note = (Note)model;

    if (note.isGrace()) {
      braille += Braille.grace;
    }

    Set<Ornament> ornaments = note.getOrnaments();
    if (ornaments != null) {
      for (Ornament ornament:note.getOrnaments())
        braille += ornament.toBraille();
    }
    Set<Articulation> articulations = note.getArticulations();
    if (articulations != null) {
      for (Articulation articulation : articulations)
        braille += articulation.toBraille();
    }

    Accidental accidental = note.getAccidental();
    if (accidental != null) {
      braille += accidental.toBraille().toString();
    }
    AbstractPitch pitch = (AbstractPitch)note.getPitch();
    if (pitch != null) {
      Braille octaveSign = pitch.getOctaveSign(lastPitch);
      if (octaveSign != null) { braille += octaveSign; }
    }
    braille += note.getAugmentedFraction().toBrailleString(pitch);

    if (options.getShowFingering()) {
      Fingering fingering = note.getFingering();
      if (fingering != null) {
        braille += fingering.toBrailleString();
      }
    }

    Fermata fermata = note.getFermata();
    if (fermata != null) {
      braille += fermata.toBraille();
    }

    if (note.isTieStart()) {
      braille += Braille.tie;
    } else {
      boolean printSlur = false;
      for (Slur<Note> slur:note.getSlurs()) {
        if (!slur.lastNote(note)) {
          printSlur = true;
          break;
        }
      }
      if (printSlur) {
        braille += Braille.slur;
      }
    }

    return braille;
  }
}
