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
package freedots.musicxml;

import freedots.music.TupletElement;
import freedots.math.Fraction;
import freedots.math.PowerOfTwo;

public class Tuplet extends freedots.music.Tuplet {
  public Tuplet() {
  }

  public final boolean addNote(final Note note) {
    if (super.add(note)) {
      note.addTuplet(this);
      return true;
    }
    return false;
  }

  public final boolean addTuplet(Tuplet tuplet) {	
    if (super.add(tuplet)) {
      tuplet.setParent(this);  //A tuplet is only in one tuplet
      return true;
    }
    return false;
  }

  void setParent(Tuplet tuplet) {
    this.parent = tuplet;
  }

  public boolean completed() {
    Fraction expectedFrac = this.getActualType().simplify();
    Fraction sumFrac = new Fraction(0,1);
    for (TupletElement tE: this ){
      Fraction currentFrac=Fraction.ZERO;
      if (tE instanceof Tuplet) {
        Tuplet tuplet = (Tuplet)tE;
        currentFrac = tuplet.getNormalType();
      } else {
        final Note note = (Note)tE;
        currentFrac =
          new Fraction(new PowerOfTwo(note.getAugmentedFraction().getPower()));
      }
      sumFrac = sumFrac.add(currentFrac);	
    }
    return sumFrac.equals(expectedFrac);
  }
}

