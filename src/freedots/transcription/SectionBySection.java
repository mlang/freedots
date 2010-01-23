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
package freedots.transcription;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import freedots.Braille;
import freedots.braille.ArtificialWholeRest;
import freedots.braille.BrailleList;
import freedots.braille.BrailleSyllable;
import freedots.braille.DoubleBarSign;
import freedots.braille.HarmonyPart;
import freedots.braille.LeftHandPart;
import freedots.braille.MusicHyphen;
import freedots.braille.MusicPart;
import freedots.braille.RightHandPart;
import freedots.braille.TextPart;
import freedots.Options;
import freedots.music.ClefChange;
import freedots.music.EndBar;
import freedots.music.Event;
import freedots.music.Fraction;
import freedots.music.GlobalKeyChange;
import freedots.music.KeyChange;
import freedots.music.KeySignature;
import freedots.music.Lyric;
import freedots.music.MusicList;
import freedots.music.Staff;
import freedots.music.StartBar;
import freedots.music.Syllabic;
import freedots.musicxml.Direction;
import freedots.musicxml.Harmony;
import freedots.musicxml.Note;
import freedots.musicxml.Part;
import freedots.musicxml.Score;

class SectionBySection implements Strategy {
  private Options options = null;
  private Score score = null;
  private Transcriber transcriber = null;

  /** Main entry point to invoke implemented transcription Strategy.
   *
   * @param transcriber is the Transcriber object to use
   */
  public void transcribe(final Transcriber transcriber) {
    this.transcriber = transcriber;
    options = transcriber.getOptions();
    score = transcriber.getScore();

    for (Part part: score.getParts()) {
      String name = part.getName();
      if (name != null && !name.isEmpty()) transcriber.printLine(name);
      List<Direction> directives = part.getDirectives();
      String directiveText = "";
      if (directives.size() == 1) {
        final Direction directive = directives.get(0);
        directiveText = directives.get(0).getWords().trim() + " ";
        transcriber.alreadyPrintedDirections.add(directive);
      }
      transcriber.printLine(directiveText
                            + part.getKeySignature().toBraille()
                            + Braille.toString(part.getTimeSignature()));
      for (Section section:getSections(part)) transcribeSection(part, section);
      if (transcriber.getCurrentColumn() > 0) transcriber.newLine();
      transcriber.newLine();
    }
  }

  /** Transcribes a section of music (possibly consisting of several staves
   *  and including lyrics and chords).
   */
  private void transcribeSection(final Part part, final Section section) {
    final int staffCount = section.getStaffCount();
    for (int staffIndex = 0; staffIndex < staffCount; staffIndex++) {
      final Staff staff = section.getStaff(staffIndex);

      if (transcriber.getCurrentColumn() > 0) transcriber.newLine();
      transcriber.indentTo(2);

      int chordDirection = -1;
      if (staffCount == 1 && staff.containsHarmony()) {
        transcriber.printString(new MusicPart());
      } else if (staffCount == 2) {
        if (staffIndex == 0) {
          transcriber.printString(new RightHandPart());
          chordDirection = -1;
        } else if (staffIndex == 1) {
          transcriber.printString(new LeftHandPart());
          chordDirection = 1;
        }
      }

      transcribeMusic(staff, chordDirection);

      if (staff.containsHarmony()) {
        if (transcriber.getCurrentColumn() > 0) transcriber.newLine();
        transcribeHarmony(staff);
      }

      String lyricText = staff.getLyricText();
      if (lyricText.length() > 0) transcribeLyrics(staff);
    }
  }

  private void transcribeMusic(Staff staff, final int chordDirection) {
    BrailleMeasure measure = new BrailleMeasure(transcriber);
    measure.setVoiceDirection(chordDirection);

    StartBar startBar = null;
    KeySignature currentSignature = staff.getKeySignature(staff.get(0).getOffset());

    for (int staffElementIndex = 0; staffElementIndex < staff.size();
         staffElementIndex++) {
      Event event = staff.get(staffElementIndex);

      if (event instanceof StartBar) {
        startBar = (StartBar)event;
        measure.setTimeSignature(startBar.getTimeSignature());
      } else if (event instanceof KeyChange) {
        KeyChange kc = (KeyChange)event;
        if (!kc.getKeySignature().equals(currentSignature)) {
          currentSignature = kc.getKeySignature();
          transcriber.printString(currentSignature.toBraille());
          transcriber.spaceOrNewLine();
        }
      } else if (event instanceof EndBar) {
        EndBar rightBar = (EndBar)event;
        int charactersLeft = transcriber.getRemainingColumns();
        if (charactersLeft <= 2) {
          transcriber.newLine();
          charactersLeft = transcriber.getRemainingColumns();
        }

        boolean lastLine = transcriber.isLastLine();
        measure.process();
        BrailleList head = measure.head(charactersLeft, lastLine);
        BrailleList tail = measure.tail();
        if (head.length() <= tail.length() / 10) {
          transcriber.newLine();
          charactersLeft = transcriber.getRemainingColumns();
          head = measure.head(charactersLeft, lastLine);
          tail = measure.tail();
        }
        if (startBar != null) {
          if (startBar.getRepeatForward()) {
            String braille = Braille.postDottedDoubleBar.toString();
            braille += Braille.unicodeBraille(Braille.dotsToBits(3));
            transcriber.printString(braille);
          }
          if (startBar.getEndingStart() > 0) {
            String braille = Braille.numberSign.toString();
            braille += Braille.lowerNumber(startBar.getEndingStart());
            braille += Braille.dot.toString();
            transcriber.printString(braille);
          }
        }
        transcriber.printString(head);
        if (tail.length() > 0) {
          transcriber.printString(Braille.hyphen.toString());
          transcriber.newLine();
          transcriber.printString(tail);
        }

        if (rightBar.getRepeat())
          transcriber.printString(Braille.dottedDoubleBar.toString());
        else if (rightBar.getEndOfMusic())
          transcriber.printString(Braille.doubleBar.toString());

        if (!rightBar.getEndOfMusic()) transcriber.spaceOrNewLine();

        measure = new BrailleMeasure(transcriber, measure);
        measure.setVoiceDirection(chordDirection);
      } else {
        measure.add(event);
      }
    }

  }
  private void transcribeLyrics(Staff staff) {
    if (transcriber.getCurrentColumn() > 0) transcriber.newLine();
    transcriber.printString(new TextPart());
    Syllabic lastSyllabic = null;
    for (Event event: staff) {
      if (event instanceof Note) {
        Lyric lyric = ((Note)event).getLyric();
        if (lyric != null) {
          String text = lyric.getText();
          if (text.length() <= transcriber.getRemainingColumns()) {
            transcriber.printString(new BrailleSyllable(text, (Note)event));
          } else {
            if (lastSyllabic != Syllabic.SINGLE
             && lastSyllabic != Syllabic.END) {
              transcriber.printString("-");
            }
            transcriber.newLine();
            transcriber.printString(new BrailleSyllable(text, (Note)event));
          }
          if (lyric.getSyllabic() == Syllabic.SINGLE
           || lyric.getSyllabic() == Syllabic.END) {
            transcriber.spaceOrNewLine();
          }
          lastSyllabic = lyric.getSyllabic();
        }
      }
    }
  }

  private void transcribeHarmony(Staff staff) {
    transcriber.printString(new HarmonyPart());
    MeasureOfHarmonies measure = new MeasureOfHarmonies();
    Iterator<Event> staffIterator = staff.iterator();
    while (staffIterator.hasNext()) {
      Event event = staffIterator.next();
      if (event instanceof Harmony) {
        measure.add(new HarmonyInfo((Harmony)event));
      } else if (event instanceof EndBar) {
        if (measure.size() > 0) {
          measure.calculateDurations(((EndBar)event).getOffset());
          boolean includeStems = !measure.isEvenRhythm();
          Iterator<HarmonyInfo> iterator = measure.iterator();
          boolean first = true;
          while (iterator.hasNext()) {
            HarmonyInfo current = iterator.next();
            String chord = Braille.toString(current.getHarmony());
            if (includeStems && iterator.hasNext())
              chord += Braille.toString(current.getDuration().decompose());

            if (chord.length() <= transcriber.getRemainingColumns())
              transcriber.printString(chord);
            else {
              if (!first) transcriber.printString(new MusicHyphen());
              transcriber.newLine();
              transcriber.printString(chord);
            }
            first = false;
          }
        } else {
          transcriber.printString(new ArtificialWholeRest());
        }
        measure.clear();
        if (staffIterator.hasNext()) transcriber.spaceOrNewLine();
        else transcriber.printString(new DoubleBarSign());
      }
    }
  }
  /** A simple container for storing calculated duration.
   */
  private class HarmonyInfo {
    private Harmony harmony;
    HarmonyInfo(final Harmony harmony) { this.harmony = harmony; }
    Harmony getHarmony() { return harmony; }
    Fraction getOffset() { return harmony.getOffset(); }

    private Fraction duration = null;
    void setDuration(final Fraction duration) { this.duration = duration; }
    Fraction getDuration() { return duration; }
  }
  private class MeasureOfHarmonies extends ArrayList<HarmonyInfo> {
    /** Calculates the durations of the various Harmony elements contained
     *  in this measure.
     * This should probably be done in the core MusicXML library instead.
     */
    void calculateDurations (final Fraction measureEnd) {
      if (size() > 0) {
        Iterator<HarmonyInfo> iterator = iterator();
        HarmonyInfo last = iterator.next();
        while (iterator.hasNext()) {
          HarmonyInfo current = iterator.next();
          last.setDuration(current.getOffset().subtract(last.getOffset()));
          last = current;
        }
        last.setDuration(measureEnd.subtract(last.getOffset()));
      }
    }  
    /** Determines if all harmonies are of the same duration.
     * @return false if any duration differs from any other.
     */
    boolean isEvenRhythm() {
      Iterator<HarmonyInfo> iterator = iterator();
      if (iterator.hasNext()) {
        HarmonyInfo last = iterator.next();
        while (iterator.hasNext()) {
          HarmonyInfo current = iterator.next();
          if (!last.getDuration().equals(current.getDuration())) return false;
          last = current;
        }
      }
      return true;
    }
  }

  /** A container class for keeping sections of music apart.
   * This should probably be moved into package {@link freedots.music}.
   */
  private class Section extends MusicList {
    private Part part;
    Section(final Part part) {
      super();
      this.part = part;
    }
    @Override
    public Staff getStaff(final int index) {
      Staff staff = super.getStaff(index);
      /* We need to populate key/clef/timeList with events from the past */
      if (staff != null && !staff.isEmpty()) {
        Fraction startOffset = staff.get(0).getOffset();
        for (Event event : part.getMusicList()) {
          if (event.getOffset().compareTo(startOffset) < 0) {
            if (event instanceof GlobalKeyChange) {
              GlobalKeyChange globalKeyChange = (GlobalKeyChange)event;
              staff.keyList.put(globalKeyChange.getOffset(),
                                globalKeyChange.getKeySignature());
            } else if (event instanceof KeyChange) {
              KeyChange keyChange = (KeyChange)event;
              if (keyChange.getStaffNumber() == index) {
                staff.keyList.put(keyChange.getOffset(),
                                  keyChange.getKeySignature());
              }
            } else if (event instanceof ClefChange) {
              ClefChange clefChange = (ClefChange)event;
              if (clefChange.getStaffNumber() == index) {
                staff.clefList.put(clefChange.getOffset(),
                                   clefChange.getClef());
              }
            }
          }
        }
      }

      return staff;
    }
  }

  private List<Section> getSections(Part part) {
    List<Section> sections = new ArrayList<Section>();
    Section currentSection = new Section(part);
    sections.add(currentSection);
    MusicList musicList = part.getMusicList();
    int index = 0;
    int measureCount = 0;

    final boolean newSystemEndsSection =
      options.getNewSystemEndsSection()
      && score.encodingSupports("print", "new-system", true);

    while (true) {
      while (index < musicList.size()) {
        Event event = musicList.get(index++);
        currentSection.add(event);
        if (event instanceof EndBar) { measureCount++; break; }
      }

      if (index == musicList.size()) return sections;

      if (!(musicList.get(index) instanceof StartBar))
        throw new RuntimeException();

      StartBar startBar = (StartBar)musicList.get(index);
      if ((startBar.getStaffCount() != currentSection.getStaffCount())
          ||
          (newSystemEndsSection && startBar.getNewSystem())
          ||
          (measureCount == options.getMeasuresPerSection())) {
        currentSection = new Section(part);
        sections.add(currentSection);
        measureCount = 0;
      }
    }
  }
}
