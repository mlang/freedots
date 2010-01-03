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
import freedots.Braille;
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
  private static final Logger log = Logger.getLogger(BarOverBar.class);

  private Options options = null;

  private BrailleStaves brailleStaves = new BrailleStaves();
  private KeySignature initialKeySignature = null;
  private TimeSignature initialTimeSignature = null;

  public void transcribe(Transcriber transcriber) {
    options = transcriber.getOptions();

    createMeasuresInBrailleStaves(transcriber.getScore());

    if (initialKeySignature != null && initialTimeSignature != null) {
      transcriber.printLine(initialKeySignature.toBraille()
                            + Braille.toString(initialTimeSignature));
    }
    int paragraph = 1;
    int startIndex = 0;
    while (startIndex < brailleStaves.getMeasureCount()) {
      String paragraphNumber = Braille.upperNumber(paragraph);
      int indent = paragraphNumber.length() + 1;

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
          BrailleList braille = measure.head(1024, false);
          if (i == startIndex) {
            if (staffIndex == 0) {
              transcriber.printString(paragraphNumber+" ");
            } else {
              for (int count = 0; count < paragraphNumber.length(); count++) {
                transcriber.printString(" ");
              }
              transcriber.printString(" ");
            }
            Braille introSymbol = brailleStaves.get(staffIndex).getIntro();
            if (introSymbol != null) {
              BrailleString intro = new BrailleString(introSymbol);
              braille.add(0, intro);
            }
          }
          transcriber.printString(braille);

          int skipColumns = columnWidth - braille.length();
          if (skipColumns > 0) {
            transcriber.printString(" ");
            skipColumns--;
          }
          if (skipColumns > 2) {
            while (skipColumns > 0) {
              transcriber.printString(Braille.dot.toString());
              skipColumns--;
            }
          }
          while (skipColumns > 0) {
            transcriber.printString(" ");
            skipColumns--;
          }
          transcriber.printString(" ");
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
          log.warning("Parts with different initial key signatures");
        }
      }
      if (initialTimeSignature == null) {
        initialTimeSignature = part.getTimeSignature();
      } else {
        if (!initialTimeSignature.equals(part.getTimeSignature())) {
          log.warning("Parts with different initial time signatures");
        }
      }

      MusicList musicList = part.getMusicList();
      int staffCount = musicList.getStaffCount();
      for (int staffIndex = 0; staffIndex < staffCount; staffIndex++) {
        Staff staff = musicList.getStaff(staffIndex);
        BrailleStaff brailleStaff = new BrailleStaff();
        BrailleMeasure measure = new BrailleMeasure();
        boolean displayClefChange = false;
        int voiceDirection = -1;

        if (staffCount == 1) {
          brailleStaff.setIntro(Braille.soloPart);
          if (staff.containsChords()) displayClefChange = true;
        } else if (staffCount == 2) {
          if (staffIndex == 0) {
            brailleStaff.setIntro(Braille.rightHandPart);
            voiceDirection = -1;
            measure.setVoiceDirection(voiceDirection);
          } else if (staffIndex == 1) {
            brailleStaff.setIntro(Braille.leftHandPart);
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
            measure = new BrailleMeasure(measure);
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
    private Braille intro = null;
    public void setIntro(Braille intro) { this.intro = intro; }
    public Braille getIntro() { return intro; }
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
