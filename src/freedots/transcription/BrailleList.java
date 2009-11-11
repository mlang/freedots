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
package freedots.transcription;

class BrailleList extends java.util.ArrayList<BrailleString> {
  @Override
  public String toString() {
    StringBuilder stringBuilder = new StringBuilder();
    for (BrailleString brailleString:this)
      stringBuilder.append(brailleString.toString());
    return stringBuilder.toString();
  }
  public boolean add(BrailleString item) {
    boolean changed = super.add(item);
    item.setContainer(this);
    return changed;
  }
  public void add(int index, BrailleString item) {
    super.add(index, item);
    item.setContainer(this);
  }
  public boolean addAll(BrailleList list) {
    boolean changed = super.addAll(list);
    for (BrailleString string : list) string.setContainer(this);
    return changed;
  }
  public BrailleString getNext(BrailleString item) {
    int index = indexOf(item);
    if (index != -1 && index < size() - 1) return get(index+1);
    return null;
  }
  public int length() { return toString().length(); }
}
