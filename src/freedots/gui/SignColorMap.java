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
package freedots.gui;

import java.awt.Color;
import java.util.HashMap;

import freedots.braille.*;

/** A map of {@link freedots.braille.Sign} subclasses to associated color
 *  values.
 * <p>
 * This is essentially a ClassMap with basic support for inheritance.
 * If a Sign is not found in the map all its superclasses up to the
 * abstract base class {@link freedots.braille.Sign} are searched and the first
 * matching mapping is returned.  If no mapping for the specified class
 * is found, the default color (black) is return.
 */
public class SignColorMap extends HashMap<Class<? extends Sign>, Color> {
  private static final Color DEFAULT_COLOR = Color.black;

  public SignColorMap() {
    super();
    initializeDefaults();
  }

  private void initializeDefaults() {
    put(AccidentalSign.class, new Color(255, 126, 0));
    put(BrailleFingering.Finger.class, new Color(205, 189, 7));
    put(BrailleNote.GraceSign.class, Color.green);
    put(BrailleNote.OrnamentSign.class, Color.green);
    put(BrailleNote.ArticulationSign.class, new Color(0, 250, 154));
    put(BarSign.class, Color.red);
    put(PitchAndValueSign.class, Color.blue);
    put(PostDottedDoubleBarSign.class, Color.red);
    put(Dot.class, new Color(95, 170, 177));
    put(OctaveSign.class, Color.pink);
    put(RestSign.class, new Color(127, 13, 255));
    put(SlurSign.class, Color.lightGray);
    put(TieSign.class, Color.lightGray);
    put(BrailleChord.ChordTieSign.class, Color.lightGray);
    put(Text.class, new Color(160, 82, 45));
    put(TextPart.class, new Color(160, 82, 45));
  }

  /** Returns the color to which the specified sign is mapped,
   *  or {@link #DEFAULT_COLOR} if this map contains no mapping for the sign
   *  or any of its superclasses.
   * @param key a {@code Class<? extends Sign>} or an instance of a subclass
   *        of {@link freedots.braille.Sign}
   * @throws NullPointerException if key is null
   * @throws IllegalArgumentException if key is not a valid Sign class or
   *         instance
   * @return the color to which the specified sign is mapped, or
   *         {@link #DEFAULT_COLOR} if this map contains no mapping for the
   *         class of the sign specified by [@code key} or any of its
   *         superclasses.
   */
  @Override public Color get(Object key) {
    if (key == null) throw new NullPointerException();
    if (key instanceof Sign) key = key.getClass();
    if (!((key instanceof Class)
       && Sign.class.isAssignableFrom((Class<?>)key)))
      throw new IllegalArgumentException(key.toString());

    for (Class<?> klazz = (Class<?>)key; klazz != null && klazz != Sign.class;
         klazz = klazz.getSuperclass()) {
      final Color color = super.get(klazz);
      if (color != null) return color;
    }
    return DEFAULT_COLOR;
  }

  public static final SignColorMap DEFAULT = new SignColorMap();
}
