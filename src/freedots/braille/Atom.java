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

/** The smallest possible unit of braille.
 * Usually consists of 1 to 3 cells, not much more.
 */
public abstract class Atom implements BrailleSequence {
  protected final String data;
  protected Atom(final String data) { this.data = data; }

  public abstract String getDescription();
  public boolean needsGuideDot(BrailleSequence next) { return false; }

  private BrailleList parent = null;
  public BrailleList getParent() { return parent; }
  public void setParent(final BrailleList parent) { this.parent = parent; }

  public Object getScoreObject() { return null; }

  public final String toString() { return data; }
  public final int length() { return data.length(); }
  public final char charAt(int index) { return data.charAt(index); }
  public final CharSequence subSequence(final int start, final int end) {
    return data.subSequence(start, end);
  }

  protected final static int dotsToBits(int dots) {
    int bits = 0;
    while (dots > 0) {
      final int number = dots % 10;
      dots /= 10;
      bits |= 1 << (number - 1);
    }
    return bits;
  }
  protected static String braille(final int dots) {
    return String.valueOf((char)(0X2800|dotsToBits(dots)));
  }
  protected static String braille(int dots1, int dots2) {
    return braille(dots1) + braille(dots2);
  }
  protected static String braille(int dots1, int dots2, int dots3) {
    return braille(dots1) + braille(dots2) + braille(dots3);
  }
}
