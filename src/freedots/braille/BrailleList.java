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

import java.util.Iterator;

/** Represents a logical unit composed of several smaller objects.
 */
public class BrailleList extends java.util.LinkedList<BrailleSequence>
                         implements BrailleSequence {
  public BrailleList() { super(); }

  private BrailleList parent = null;
  public final BrailleList getParent() { return parent; }
  public final void setParent(final BrailleList parent) {
    if (parent == null) throw new NullPointerException();
    if (this.parent != null)
      throw new IllegalStateException("Parent already set");

    this.parent = parent;
  }

  /** Append a sign.
   * This method takes care of inserting {@link GuideDot} objects if
   * required.
   * @see BrailleSequence#needsGuideDot
   */
  @Override public boolean add(final BrailleSequence item) {
    if (!isEmpty() && getLast().needsGuideDot(item)) {
      final BrailleSequence dot = new GuideDot();
      dot.setParent(this);
      if (!super.add(dot)) return false;
    }
    item.setParent(this);
    return super.add(item);
  }
  @Override public void addLast(final BrailleSequence item) { add(item); }

  public String getDescription() {
    return "Groups several signs as a logical unit.";
  }

  /** Checks if the last element of this list needs a guide dot after it.
   */
  public boolean needsGuideDot(BrailleSequence next) {
    return !isEmpty() && getLast().needsGuideDot(next);
  }
  public Object getScoreObject() { return null; }

  @Override public String toString() {
    return this.appendTo(new StringBuilder()).toString();
  }
  public StringBuilder appendTo(StringBuilder sb) {
    for (BrailleSequence seq: this) seq.appendTo(sb);
    return sb;
  }
  public int length() {
    int chars = 0;
    for (BrailleSequence seq: this) chars += seq.length();
    return chars;
  }
  public char charAt(int index) { return toString().charAt(index); }
  public CharSequence subSequence(int start, int end) {
    return toString().subSequence(start, end);
  }

  /** Retrieves the {@code Sign} at index.
   */
  public Sign getSignAtIndex(final int index) {
    final Iterator<BrailleSequence> iterator = iterator();
    int pos = 0;
    while (iterator.hasNext()) {
      final BrailleSequence current = iterator.next();
      final int length = current.length();
      if (pos + length > index)
        return (current instanceof BrailleList)
          ? ((BrailleList)current).getSignAtIndex(index - pos)
          : (Sign)current;
      else pos += length;
    }
    return null;
  }
  /** Retrieves the visual score object responsible for the braille at index.
   * @see #getSignAtIndex
   */
  public Object getScoreObjectAtIndex(final int index) {
    for (BrailleSequence sign = getSignAtIndex(index); sign != null;
         sign = sign.getParent()) {
      final Object object = sign.getScoreObject();
      if (object != null) return object;
    }
    return null;
  }

  /**
   * @return -1 if the given score object was not found
   */
  public int getIndexOfScoreObject(final Object scoreObject) {
    if (scoreObject == null)
      throw new NullPointerException("Trying to search for null");

    final Iterator<BrailleSequence> iterator = iterator();
    int index = 0;
    while (iterator.hasNext()) {
      final BrailleSequence current = iterator.next();
      if (current.getScoreObject() == scoreObject) return index;
      if (current instanceof BrailleList) {
        final BrailleList compound = (BrailleList)current;
        final int subIndex = compound.getIndexOfScoreObject(scoreObject);
        if (subIndex >= 0) return index + subIndex;
      }
      index += current.length();
    }

    return -1;
  }

  /** Determines the string length from the last occurence of a class.
   * <p>
   * This method does a deep search.
   * @return -1 if class was not found in this list or its children
   */
  public final int lengthSince(Class<? extends BrailleSequence> seqClass) {
    int length = 0;
    java.util.ListIterator<BrailleSequence> iterator = listIterator(size());
    while (iterator.hasPrevious()) {
      BrailleSequence seq = iterator.previous();
      if (seq.getClass() == seqClass) return length;
      if (seq instanceof BrailleList) {
        final int subLength = ((BrailleList)seq).lengthSince(seqClass);
        if (subLength != -1) return length + subLength;
      }
      length += seq.length();
    }
    return -1;
  }
}
