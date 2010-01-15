/* -*- c-basic-offset: 2; indent-tabs-mode: nil; -*- */
/*
 * FreeDots -- MusicXML to braille music transcription
 *
 * Copyright 2008-2010 Mario Lang  All Rights Reserved.
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
package freedots.transcription;


import freedots.Braille;
import freedots.Options;
import freedots.music.AbstractPitch;
import freedots.music.Accidental;
import freedots.music.Articulation;
import freedots.music.Fermata;
import freedots.music.Ornament;
import freedots.music.Slur;
import freedots.musicxml.Note;

class BrailleNote extends BrailleString {
  private AbstractPitch lastPitch;
  private Options options;

  BrailleNote(final Note note, final AbstractPitch lastPitch) {
    super((String)null, note);
    this.lastPitch = lastPitch;
    this.options = Options.getInstance();
  }

  AbstractPitch getPitch() {
    Note note = (Note)getModel();
    return note.getPitch();
  }

  @Override public String toString() {
    String braille = "";
    Note note = (Note)getModel();

    if (note.isGrace()) braille += Braille.grace;

    for (Ornament ornament: note.getOrnaments())
      braille += ornament.toBraille();
    for (Articulation articulation: note.getArticulations())
      braille += articulation.toBraille();

    Accidental accidental = note.getAccidental();
    if (accidental != null) {
      braille += Braille.valueOf(accidental);
    }
    AbstractPitch pitch = (AbstractPitch)note.getPitch();
    if (pitch == null)
      pitch = (AbstractPitch)note.getUnpitched();
    if (pitch != null) {
      Braille octaveSign = pitch.getOctaveSign(lastPitch);
      if (octaveSign != null) braille += octaveSign;
    }
    braille += Braille.toString(note.getAugmentedFraction(), pitch);

    if (options.getShowFingering()) {
      braille += Braille.toString(note.getFingering());
    }

    Fermata fermata = note.getFermata();
    if (fermata != null) braille += fermata.toBraille();

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
