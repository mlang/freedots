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
package freedots.music;

import freedots.math.Fraction;

public abstract class Tuplet extends java.util.LinkedList<TupletElement>
  implements TupletElement {
  protected Tuplet parent = null; //if parent is null, the tuplet is the main tuplet
  protected Fraction actualType = null;
  protected Fraction normalType = null;

  public Tuplet() {
    super();
  }

  public boolean isFirstOfTuplet(TupletElement tE) {
    try {
      return getFirst() == tE;
    } catch (java.util.NoSuchElementException e) {
      return false;
    }
  }

  public int getType() { return getActualType().numerator(); }

  public Tuplet getParent() { return parent; }
  public void setParent(Tuplet tuplet) {
    if (this.parent != null) throw new AssertionError("Trying to reparent tuplet");
    this.parent = tuplet;
  }

  public void setActualType(Fraction actualType) {
    this.actualType = actualType;
  }

  public void setNormalType(Fraction normalType) {
    this.normalType = normalType;
  }

  public Fraction getActualType() {
    if (actualType == null) {
      setActualType(getActual());
    }
    return actualType;
  }

  public Fraction getNormalType() {
    if (normalType == null) {
      setNormalType(getNormal());
    }
    return normalType;
  }
   
  public final boolean addTuplet(Tuplet tuplet) {
    if (super.add(tuplet)) {
      tuplet.setParent(this);  //A tuplet is only in one tuplet
      return true;
    }
    return false;
  }

  /** Calcule NormalType with time-modification when it's not ambiguous 
   */ 
  protected abstract Fraction getNormal();

  /** Calcule ActualType with time-modification when it's not ambiguous 
   */ 
  protected abstract Fraction getActual();
}
