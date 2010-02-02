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

import java.util.Map;

import freedots.math.Fraction;

/** A container for keeping track of the current value of a certain quantity
 *  which changes over time.
 */
@SuppressWarnings("serial")
public class Timeline<E> extends java.util.TreeMap<Fraction, E> {
  /** Construct a new (empty) Timeline.
   */
  public Timeline() { super(); }
  /** Construct a Timeline initialised with the given value.
   * @param initial is the value at offset 0.
   */
  public Timeline(final E initial) { super(); put(Fraction.ZERO, initial); }

  /** Retrieves the active value at a certain offset.
   * @see #floorEntry
   */
  @Override public E get(Object key) {
    if (key instanceof Fraction) {
      Fraction offset = (Fraction)key;
      Map.Entry<Fraction, E> entry = floorEntry(offset);
      if (entry != null) return entry.getValue();
      return null;
    }
    throw new IllegalArgumentException();
  }
}
