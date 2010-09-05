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
import java.util.logging.Logger;

import freedots.math.Fraction;
import freedots.music.Accidental;
import freedots.music.AugmentedPowerOfTwo;
import freedots.musicxml.Harmony;

/** Braille representation of a harmony chord.
 */
public class BrailleHarmony extends BrailleList {
  private static final Logger LOG = Logger.getLogger(BrailleHarmony.class.getName());
  private final Harmony harmony;
  private final boolean withStems;
  public BrailleHarmony(final Harmony harmony,
                        final boolean withStems, final Fraction duration) {
    this.harmony = harmony;
    this.withStems = withStems;

    for (Harmony.HarmonyChord chord: harmony.getChords()) {
      final String kind = chord.getKind();
      if ("none".equals(kind)) {
        add(new UpcaseSign());
        add(new Text("nc"));
      } else {
        add(new UpcaseSign());
        add(new StepLetterSign(chord.getRootStep()));
        addAccidentalFromAlter(chord.getRootAlter());
        if ("major".equals(kind))
          ;
        else if ("minor".equals(kind))
          add(new MinorSign());
        else if ("augmented".equals(kind)) {
          add(new AccidentalSign(Accidental.SHARP));
          add(new UpperNumber(5));
        } else if ("diminished".equals(kind))
          add(new Text("dim"));
        else if ("dominant".equals(kind))
          add(new UpperNumber(7));
        else if ("suspended-second".equals(kind)) {
          add(new Text("sus"));
          add(new UpperNumber(2));
        } else if ("suspended-fourth".equals(kind)) {
          add(new Text("sus"));
          add(new UpperNumber(4));
        } else if ("major-sixth".equals(kind))
          add(new UpperNumber(6));
        else if ("major-seventh".equals(kind)) {
          add(new Text("maj"));
          add(new UpperNumber(7));
        } else if ("minor-seventh".equals(kind)) {
          add(new MinorSign());
          add(new UpperNumber(7));
        } else if ("diminished-seventh".equals(kind)) {
          add(new Text("dim"));
          add(new UpperNumber(7));
        } else if ("augmented-seventh".equals(kind)) {
          add(new AccidentalSign(Accidental.SHARP));
          add(new UpperNumber(5));
          add(new UpperNumber(7));
        } else if ("major-ninth".equals(kind)) {
          add(new Text("maj"));
          add(new UpperNumber(9));
        } else if ("minor-ninth".equals(kind)) {
          add(new MinorSign());
          add(new UpperNumber(9));
        } else if ("dominant-ninth".equals(kind))
          add(new UpperNumber(9));
        else if ("major-11th".equals(kind)) {
          add(new Text("maj"));
          add(new UpperNumber(11));
        } else if ("minor-11th".equals(kind)) {
          add(new MinorSign());
          add(new UpperNumber(11));
        } else if ("dominant-11th".equals(kind))
          add(new UpperNumber(11));
        else if ("dominant-13th".equals(kind))
          add(new UpperNumber(13));
        else LOG.warning("Unhandled harmony-chord kind '"+kind+"'");

        for (Harmony.HarmonyChord.Degree degree: chord.getAlterations()) {
          addAccidentalFromAlter(degree.getAlter());
          add(new UpperNumber(degree.getValue()));
        }
        if (chord.hasBass()) {
          add(new SlashSign());
          add(new StepLetterSign(chord.getBassStep()));
          addAccidentalFromAlter(chord.getBassAlter());
        }
      }
    }

    if (withStems && duration != null) {
      Iterator<AugmentedPowerOfTwo> iterator =
        AugmentedPowerOfTwo.decompose(duration, AugmentedPowerOfTwo.SEMIBREVE)
        .iterator();
      while (iterator.hasNext()) {
        final AugmentedPowerOfTwo v = iterator.next();
        switch (v.getPower()) {
        case 0: add(new WholeStemSign()); break;
        case -1: add(new HalfStemSign()); break;
        case -2: add(new QuarterStemSign()); break;
        case -3: add(new EighthStemSign()); break;
        case -4: add(new SixteenthStemSign()); break;
        case -5: add(new ThirtysecondthStemSign()); break;
        default: LOG.warning("Unmapped power of two: " + v);
        }
        for (int i = 0; i < v.dots(); i++) add(new Dot());

        if (iterator.hasNext()) add(new SlurSign());
      }
    }
  }

  static class UpcaseSign extends Sign {
    UpcaseSign() { super(braille(46)); }
    public String getDescription() { return "Upper case sign"; }
  }
  static class StepLetterSign extends Sign {
    StepLetterSign(final int step) {
      super(getSign(step));
    }
    public String getDescription() {
      return "Symbol indicating a certain root step";
    }
    private static String getSign(final int step) {
      return ENGLISH_STEP_LETTERS[step];
    }
    private final static String[] ENGLISH_STEP_LETTERS = {
      braille(14), braille(145), braille(15), braille(124), braille(1245),
      braille(1), braille(12)
    };
  }
  static class MinorSign extends Sign {
    MinorSign() { super(braille(134)); }
    public String getDescription() { return "Indicates this chord is minor"; }
  }
  static class SlashSign extends Sign {
    SlashSign() { super(braille(5, 2)); }
    public String getDescription() {
      return "A slash indicating that a bass note follows";
    }
  }
  private void addAccidentalFromAlter(final float alter) {
    if (alter == 0) return;
    add(new AccidentalSign(Accidental.fromAlter(alter)));
  }
  static class StemSign extends Sign {
    StemSign(final String sign) { super(sign); }
    public String getDescription() {
      return "Indicates a certain durational value";
    }
  }
  static class WholeStemSign extends StemSign {
    WholeStemSign() { super(braille(456, 3)); }
  }
  static class HalfStemSign extends StemSign {
    HalfStemSign() { super(braille(456, 13)); }
  }
  static class QuarterStemSign extends StemSign {
    QuarterStemSign() { super(braille(456, 1)); }
  }
  static class EighthStemSign extends StemSign {
    EighthStemSign() { super(braille(456, 12)); }
  }
  static class SixteenthStemSign extends StemSign {
    SixteenthStemSign() { super(braille(456, 123)); }
  }
  static class ThirtysecondthStemSign extends StemSign {
    ThirtysecondthStemSign() { super(braille(456, 2)); }
  }
}
