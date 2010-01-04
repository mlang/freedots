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
import java.util.List;

import freedots.Braille;
import freedots.Options;
import freedots.music.ClefChange;
import freedots.music.EndBar;
import freedots.music.Event;
import freedots.music.Fraction;
import freedots.music.GlobalKeyChange;
import freedots.music.KeyChange;
import freedots.music.Lyric;
import freedots.music.MusicList;
import freedots.music.Staff;
import freedots.music.StartBar;
import freedots.music.Syllabic;
import freedots.musicxml.Harmony;
import freedots.musicxml.Note;
import freedots.musicxml.Part;
import freedots.musicxml.Score;

class SectionBySection implements Strategy {
  private Options options = null;
  private Score score = null;

  /** Main entry point to invoke implemented transcription Strategy.
   *
   * @param transcriber is the Transcriber object to use
   */
  public void transcribe(final Transcriber transcriber) {
    options = transcriber.getOptions();
    score = transcriber.getScore();

    for (Part part: score.getParts()) {
      String name = part.getName();
      if (name != null && !name.isEmpty()) transcriber.printLine(name);
      List<String> directives = part.getDirectives();
      String directive = "";
      if (directives.size() == 1) {
        directive = directives.get(0);
      }
      transcriber.printLine(directive
                            + part.getKeySignature().toBraille()
                            + Braille.toString(part.getTimeSignature()));
      for (Section section:getSections(part)) {
        int staffCount = section.getStaffCount();
        for (int staffIndex = 0; staffIndex < staffCount; staffIndex++) {
          Staff staff = section.getStaff(staffIndex);
          BrailleMeasure measure = new BrailleMeasure();
          int voiceDirection = -1;

          if (transcriber.getCurrentColumn() > 0) transcriber.newLine();
          transcriber.indentTo(2);

          if (staffCount == 1 && staff.containsHarmony()) {
            transcriber.printString(Braille.musicPart);
          } else if (staffCount == 2) {
            if (staffIndex == 0) {
              transcriber.printString(Braille.rightHandPart);
              voiceDirection = -1;
              measure.setVoiceDirection(voiceDirection);
            } else if (staffIndex == 1) {
              transcriber.printString(Braille.leftHandPart);
              voiceDirection = 1;
              measure.setVoiceDirection(voiceDirection);
            }
          }

          StartBar startBar = null;

          for (int staffElementIndex = 0; staffElementIndex < staff.size();
               staffElementIndex++) {
            Event event = staff.get(staffElementIndex);

            if (event instanceof StartBar) {
              startBar = (StartBar)event;
              measure.setTimeSignature(startBar.getTimeSignature());
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

              if (!rightBar.getEndOfMusic()) transcriber.printString(" ");

              measure = new BrailleMeasure(measure);
              measure.setVoiceDirection(voiceDirection);
            } else {
              measure.add(event);
            }
          }

          if (staff.containsHarmony()) {
            if (transcriber.getCurrentColumn() > 0) transcriber.newLine();
            transcriber.printString(Braille.harmonyPart);
            for (Event event: staff) {
              if (event instanceof Harmony) {
                Harmony harmony = (Harmony)event;
                String chord = Braille.toString(harmony);
                if (chord.length() <= transcriber.getRemainingColumns())
                  transcriber.printString(Braille.toString(harmony));
                else {
                  transcriber.newLine();
                  transcriber.printString(Braille.toString(harmony));
                }
              } else if (event instanceof EndBar) {
                transcriber.printString(" ");
              }
            }
          }

          String lyricText = staff.getLyricText();
          if (lyricText.length() > 0) {
            if (transcriber.getCurrentColumn() > 0) transcriber.newLine();
            transcriber.printString(Braille.textPart);
            Syllabic lastSyllabic = null;
            for (Event event: staff) {
              if (event instanceof Note) {
                Lyric lyric = ((Note)event).getLyric();
                if (lyric != null) {
                  String text = lyric.getText();
                  if (text.length() <= transcriber.getRemainingColumns()) {
                    transcriber.printString(new BrailleString(text, event));
                  } else {
                    if (lastSyllabic != Syllabic.SINGLE
                     && lastSyllabic != Syllabic.END) {
                      transcriber.printString("-");
                    }
                    transcriber.newLine();
                    transcriber.printString(new BrailleString(text, event));
                  }
                  if (lyric.getSyllabic() == Syllabic.SINGLE
                   || lyric.getSyllabic() == Syllabic.END) {
                    if (transcriber.getRemainingColumns() <= 0)
                      transcriber.newLine();
                    else
                      if (transcriber.getCurrentColumn() >= 0)
                        transcriber.printString(" ");
                  }
                  lastSyllabic = lyric.getSyllabic();
                }
              }
            }
          }
        }
      }
      if (transcriber.getCurrentColumn() > 0) transcriber.newLine();
      transcriber.newLine();
    }
  }

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
