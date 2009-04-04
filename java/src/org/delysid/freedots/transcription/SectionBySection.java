/* -*- c-basic-offset: 2; -*- */
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
package org.delysid.freedots.transcription;

import java.util.ArrayList;
import java.util.List;

import org.delysid.freedots.Braille;
import org.delysid.freedots.Options;
import org.delysid.freedots.model.Clef;
import org.delysid.freedots.model.ClefChange;
import org.delysid.freedots.model.EndBar;
import org.delysid.freedots.model.Event;
import org.delysid.freedots.model.Fraction;
import org.delysid.freedots.model.GlobalKeyChange;
import org.delysid.freedots.model.KeyChange;
import org.delysid.freedots.model.KeySignature;
import org.delysid.freedots.model.MusicList;
import org.delysid.freedots.model.Staff;
import org.delysid.freedots.model.StartBar;
import org.delysid.freedots.musicxml.Part;

class SectionBySection implements Strategy {
  private Options options = null;

  public void transcribe(Transcriber transcriber) {
    options = transcriber.getOptions();

    for (Part part:transcriber.getScore().getParts()) {
      transcriber.printLine(part.getName());
      transcriber.printLine(part.getKeySignature().toBraille() +
			    part.getTimeSignature().toBraille());
      for (Section section:getSections(part)) {
        int staffCount = section.getStaffCount();
        for (int staffIndex = 0; staffIndex < staffCount; staffIndex++) {
	  Staff staff = section.getStaff(staffIndex);
	  BrailleMeasure measure = new BrailleMeasure();
          boolean displayClefChange = false;
          int voiceDirection = -1;

          if (transcriber.getCurrentColumn() > 0) transcriber.newLine();
          transcriber.indentTo(2);

          if (staffCount == 1 && staff.containsChords()) {
            displayClefChange = true;
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

          String lyric = staff.getLyricText();
          if (lyric.length() > 0) transcriber.printLine(lyric);

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
                  braille += Braille.lowerDigit(startBar.getEndingStart());
                  braille += Braille.unicodeBraille(Braille.dotsToBits(3));
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

              transcriber.printString(" ");

              measure = new BrailleMeasure(measure);
              measure.setVoiceDirection(voiceDirection);
            } else {
              measure.add(event);
            }
          }
        }
      }
      if (transcriber.getCurrentColumn() > 0) transcriber.newLine();
      transcriber.newLine();
    }
  }

  class Section extends MusicList {
    private Part part;
    Section(Part part) {
      super();
      this.part = part;
    }
    @Override
    public Staff getStaff(int index) {
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

    boolean systemBreakVisual = options.multiStaffMeasures == Options.MultiStaffMeasures.VISUAL;

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
      if ((startBar.getStaffCount() != currentSection.getStaffCount()) ||
          (currentSection.getStaffCount() > 1 && (
           (systemBreakVisual && startBar.getNewSystem()) ||
           (options.multiStaffMeasures == Options.MultiStaffMeasures.TWO &&
            measureCount == 2) ||
           (options.multiStaffMeasures == Options.MultiStaffMeasures.THREE &&
            measureCount == 3) ||
           (options.multiStaffMeasures == Options.MultiStaffMeasures.FOUR &&
            measureCount == 4) ||
           (options.multiStaffMeasures == Options.MultiStaffMeasures.FIVE &&
            measureCount == 5) ||
           (options.multiStaffMeasures == Options.MultiStaffMeasures.SIX &&
            measureCount == 6) ||
           (options.multiStaffMeasures == Options.MultiStaffMeasures.SEVEN &&
            measureCount == 7) ||
           (options.multiStaffMeasures == Options.MultiStaffMeasures.EIGHT &&
            measureCount == 8) ||
           (options.multiStaffMeasures == Options.MultiStaffMeasures.NINE &&
            measureCount == 9) ||
           (options.multiStaffMeasures == Options.MultiStaffMeasures.TEN &&
            measureCount == 10) ||
           (options.multiStaffMeasures == Options.MultiStaffMeasures.ELEVEN &&
            measureCount == 11) ||
           (options.multiStaffMeasures == Options.MultiStaffMeasures.TWELVE &&
            measureCount == 12))) ||
          (currentSection.getLyricText().length() >= options.getPageWidth())) {
	currentSection = new Section(part);
	sections.add(currentSection);
        measureCount = 0;
      }
    }
  }
}
