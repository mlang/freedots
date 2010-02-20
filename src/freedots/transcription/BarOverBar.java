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
import freedots.braille.BrailleKeySignature;
import freedots.braille.BrailleList;
import freedots.braille.BrailleSequence;
import freedots.braille.BrailleTimeSignature;
import freedots.braille.Dot;
import freedots.braille.LeftHandPart;
import freedots.braille.RightHandPart;
import freedots.braille.Sign;
import freedots.braille.SoloPart;
import freedots.braille.Space;
import freedots.braille.UpperDigits;
import freedots.logging.Logger;
import freedots.Options;
import freedots.music.EndBar;
import freedots.music.Event;
import freedots.music.KeySignature;
import freedots.music.MusicList;
import freedots.music.Staff;
import freedots.music.StartBar;
import freedots.music.TimeSignature;
import freedots.musicxml.Part;
import freedots.musicxml.Score;

class BarOverBar implements Strategy {
  private static final Logger LOG = Logger.getLogger(BarOverBar.class);

  private Transcriber transcriber;
  private Options options = null;

  private BrailleStaves brailleStaves = new BrailleStaves();
  private KeySignature initialKeySignature = null;
  private TimeSignature initialTimeSignature = null;

  public void transcribe(Transcriber transcriber) {
    this.transcriber = transcriber;
    options = transcriber.getOptions();

    createMeasuresInBrailleStaves(transcriber.getScore());

    if (initialKeySignature != null && initialTimeSignature != null) {
      transcriber.printString(new BrailleKeySignature(initialKeySignature));
      transcriber.printString(new BrailleTimeSignature(initialTimeSignature));
      transcriber.newLine();
    }
    int paragraph = 1;
    int startIndex = 0;
    while (startIndex < brailleStaves.getMeasureCount()) {
      BrailleSequence paragraphNumber = new UpperDigits(paragraph);
      final int indent = paragraphNumber.length() + 1;

      int endIndex = startIndex + 1;
      while (endIndex <= brailleStaves.getMeasureCount()
             && (brailleStaves.maxLength(startIndex, endIndex)+indent
                 < transcriber.getRemainingColumns())) {
        endIndex++;
      }
      if (endIndex > startIndex + 1) endIndex--;
      /* Header? */
      for (int staffIndex = 0; staffIndex < brailleStaves.size();
           staffIndex++) {
        for (int i = startIndex; i < endIndex; i++) {
          int columnWidth = brailleStaves.maxLengthAt(i);
          BrailleMeasure measure = brailleStaves.get(staffIndex).get(i);
          if (i == startIndex) measure.unlinkPrevious();

          BrailleList measureBraille = measure.head(1024, false);
          BrailleList braille = new BrailleList();
          if (i == startIndex) {
            if (staffIndex == 0) {
              transcriber.printString(paragraphNumber);
              transcriber.printString(new Space());
            } else {
              transcriber.indentTo(indent);
            }
            Sign introSymbol = brailleStaves.get(staffIndex).getIntro();
            if (introSymbol != null) {
              braille.add((Sign)introSymbol.clone());
            }
          }
          braille.add(measureBraille);
          transcriber.printString(braille);

          int skipColumns = columnWidth - braille.length();
          if (skipColumns > 0) {
            transcriber.printString(new Space());
            skipColumns--;
          }
          if (skipColumns > 2) {
            while (skipColumns > 0) {
              transcriber.printString(new Dot() {
                                        @Override public String getDescription() {
                                          return "Alignment dot";
                                        }
                                      });
              skipColumns--;
            }
          }
          while (skipColumns > 0) {
            transcriber.printString(new Space());
            skipColumns--;
          }
          transcriber.printString(new Space());
        }
        transcriber.newLine();
      }

      paragraph++;
      startIndex = endIndex;
    }
  }

  private void createMeasuresInBrailleStaves(Score score) {
    brailleStaves.clear();
    initialTimeSignature = null;
    initialKeySignature = null;

    for (Part part: score.getParts()) {
      if (initialKeySignature == null) {
        initialKeySignature = part.getKeySignature();
      } else {
        if (!initialKeySignature.equals(part.getKeySignature())) {
          LOG.warning("Parts with different initial key signatures");
        }
      }
      if (initialTimeSignature == null) {
        initialTimeSignature = part.getTimeSignature();
      } else {
        if (!initialTimeSignature.equals(part.getTimeSignature())) {
          LOG.warning("Parts with different initial time signatures");
        }
      }

      MusicList musicList = part.getMusicList();
      int staffCount = musicList.getStaffCount();
      for (int staffIndex = 0; staffIndex < staffCount; staffIndex++) {
        Staff staff = musicList.getStaff(staffIndex);
        BrailleStaff brailleStaff = new BrailleStaff();
        BrailleMeasure measure = new BrailleMeasure(transcriber);
        boolean displayClefChange = false;
        int voiceDirection = -1;

        if (staffCount == 1) {
          brailleStaff.setIntro(new SoloPart());
          if (staff.containsChords()) displayClefChange = true;
        } else if (staffCount == 2) {
          if (staffIndex == 0) {
            brailleStaff.setIntro(new RightHandPart());
            voiceDirection = -1;
            measure.setVoiceDirection(voiceDirection);
          } else if (staffIndex == 1) {
            brailleStaff.setIntro(new LeftHandPart());
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
            measure.process();
            brailleStaff.add(measure);
            measure = new BrailleMeasure(transcriber, measure);
            measure.setVoiceDirection(voiceDirection);
          } else {
            measure.add(event);
          }
        }
        brailleStaves.add(brailleStaff);
      }
    }
  }

  private class BrailleStaff extends ArrayList<BrailleMeasure> {
    private Sign intro = null;
    void setIntro(Sign intro) { this.intro = intro; }
    Sign getIntro() { return intro; }
  }
  @SuppressWarnings("serial")
  private class BrailleStaves extends ArrayList<BrailleStaff> {
    int maxLength(int from, int to) {
      int length = 0;
      for (int i = from; i < to; i++) {
        length += maxLengthAt(i);
      }
      return length + (to - from);
    }
    int maxLengthAt(int index) {
      int maxLength = 0;
      for (BrailleStaff brailleStaff : this) {
        BrailleMeasure measure = brailleStaff.get(index);
        BrailleList braille = measure.head(1024, false);
        maxLength = Math.max(maxLength, braille.length());
      }
      return maxLength;
    }
    int getMeasureCount() {
      if (isEmpty()) return 0;
      return get(0).size();
    }
  }
}
