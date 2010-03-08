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

import java.util.ListIterator;

public class Slur<T extends VoiceElement> extends java.util.LinkedList<T> {
  public Slur(final T initialNote) {
    super();
    add(initialNote);
  }

  public boolean lastNote(T note) {
    return indexOf(note) == size() - 1;
  }
  // TODO: Check for staff and voice, do not use getFirst()
  public boolean isFirst(T note) {
    try {
      return getFirst() == note;
    } catch (java.util.NoSuchElementException e) {
      return false;
    }
  }
  // TODO: Check for staff and voice, do not use getLast()
  public boolean isLast(T note) {
    try {
      return getLast() == note;
    } catch (java.util.NoSuchElementException e) {
      return false;
    }
  }
  /** Counts the number of slur arcs which belong to same staff and voice.
   */
  public int countArcs(T note) {
    int count = 0;
    final int pos = indexOf(note);
    ListIterator<T> iter = listIterator(pos);
    while (iter.hasNext()) {
      T e = iter.next();
      if (equalsStaffAndVoice(note, e)) count += 1; else break;
    }
    iter = listIterator(pos);
    while (iter.hasPrevious()) {
      T e = iter.previous();
      if (equalsStaffAndVoice(note, e)) count += 1; else break;
    }
    return count;
  }
  protected static boolean equalsStaffAndVoice(VoiceElement e1, VoiceElement e2) {
    return (e1.getStaffNumber()==e2.getStaffNumber())
        || (e1.getVoiceName()==null && e2.getVoiceName()==null)
        || (e1.getVoiceName()!=null && e1.getVoiceName().equals(e2.getVoiceName()));
  }
}

