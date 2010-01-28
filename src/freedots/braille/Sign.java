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
 *
 * @see <a href="http://en.wikipedia.org/wiki/Sign_(semiotics)">Wikipedia:
 *      Sign (semiotics)</a>
 */
public abstract class Sign implements BrailleSequence, Cloneable {
  protected final String data;
  protected Sign(final String data) { this.data = data; }

  public abstract String getDescription();
  public boolean needsGuideDot(BrailleSequence next) { return false; }

  private BrailleList parent = null;
  public final BrailleList getParent() { return parent; }
  public final void setParent(final BrailleList parent) {
    if (parent == null) throw new NullPointerException();
    if (this.parent != null)
      throw new IllegalStateException("Parent already set");

    this.parent = parent;
  }

  public Object getScoreObject() { return null; }

  public final String toString() { return data; }
  public final StringBuilder appendTo(StringBuilder sb) {
    return sb.append(data);
  }
  public final int length() { return data.length(); }
  public final char charAt(int index) { return data.charAt(index); }
  public final CharSequence subSequence(final int start, final int end) {
    return data.subSequence(start, end);
  }

  @Override public Sign clone() {
    try {
      Sign newSign = (Sign)super.clone();
      newSign.parent = null;

      return newSign;
    } catch(CloneNotSupportedException e) {
      throw new InternalError();
    }
  }

  /** Converts a braille dot pattern to ISO 11548-1.
   * <p>
   * In the source code braille patterns are specified with a decimal encoding
   * for maximum readability and maintainability.
   * The integer 123456 represents a 6-dot pattern with all dots set.
   * Empty cells are represented with 0.
   *
   * @return ISO 11548-1 encoding of braille pattern
   */
  protected final static int dotsToBits(final int dots) {
    int bits = 0;
    for (int decimal = dots; decimal > 0; decimal /= 10) {
      bits |= 1 << ((decimal % 10) - 1);
    }
    return bits;
  }
  protected static String braille(final int dots) {
    return String.valueOf((char)(UNICODE_BRAILLE_MASK|dotsToBits(dots)));
  }
  protected static String braille(int dots1, int dots2) {
    return braille(dots1) + braille(dots2);
  }
  protected static String braille(int dots1, int dots2, int dots3) {
    return braille(dots1) + braille(dots2) + braille(dots3);
  }

  /** Start of Unicode braille range.
   * @see <a href="http://www.unicode.org/charts/PDF/U2800.pdf">Unicode range
   *      U+2800 to U+28FF</a>
   */
  protected static final int UNICODE_BRAILLE_MASK = 0X2800;
}
