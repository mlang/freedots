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
package freedots.braille;

import java.awt.Color;

/** Identifies a sequence of Unicode braille characters.
 */
public interface BrailleSequence extends CharSequence {
  /** Appends the content of this sequence to a StringBuilder.
   */
  StringBuilder appendTo(StringBuilder stringBuilder);

  /** Gets a human readable description of this sequence.
   */
  String getDescription();

  /** Checks if a {@link freedots.braille.GuideDot} needs to be
   *  inserted between this and the next sequence.
   */
  boolean needsGuideDot(BrailleSequence next);

  /** Gets the parent of this sign if it is part of a
   *  {@link freedots.braille.BrailleList}.
   * @return null if this sign has not been added to a parent yet
   */
  BrailleList getParent();

  /** Sets the {@link freedots.braille.BrailleList} which contains this sign.
   * <p>
   * Note that this method can only be called once to avoid accidentally
   * trying to add a sequence to several parents.
   */
  void setParent(BrailleList parent);

  /** Gets the visual score object responsible for the creation of this sign.
   */
  Object getScoreObject();
  
  Color getSignColor();
}
