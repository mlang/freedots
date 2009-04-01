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
import org.delysid.freedots.musicxml.Score;
import org.delysid.freedots.musicxml.Part;

class BarOverBar implements Strategy {
  private Options options = null;

  public void transcribe(Transcriber transcriber) {
    options = transcriber.getOptions();

    BrailleStaves brailleStaves = new BrailleStaves(transcriber.getScore().getParts().size());

    for (Part part:transcriber.getScore().getParts()) {
      MusicList musicList = part.getMusicList();
      int staffCount = musicList.getStaffCount();
      for (int staffIndex = 0; staffIndex < staffCount; staffIndex++) {
	Staff staff = musicList.getStaff(staffIndex);
	BrailleStaff brailleStaff = new BrailleStaff();
	BrailleMeasure measure = new BrailleMeasure();
	boolean displayClefChange = false;
	int voiceDirection = -1;

	if (staffCount == 1) {
	  brailleStaff.setIntro(Braille.soloPart.toString());
	  if (staff.containsChords()) displayClefChange = true;
	} else if (staffCount == 2) {
	  if (staffIndex == 0) {
	    brailleStaff.setIntro(Braille.rightHandPart.toString());
	    voiceDirection = -1;
	    measure.setVoiceDirection(voiceDirection);
	  } else if (staffIndex == 1) {
	    brailleStaff.setIntro(Braille.leftHandPart.toString());
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
	    if (startBar != null) {
	    }
	    if (rightBar.getRepeat()) {
	    } else if (rightBar.getEndOfMusic()) {
	    }
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

    int paragraph = 1;
    int startIndex = 0;
    while (startIndex < brailleStaves.getMeasureCount()) {
      String paragraphNumber = Braille.upperNumber(paragraph);
      int indent = paragraphNumber.length() + 1;

      int endIndex = startIndex + 1;
      while (endIndex <= brailleStaves.getMeasureCount() &&
	     brailleStaves.maxLength(startIndex, endIndex)+indent < transcriber.getRemainingColumns()) {
	endIndex++;
      }
      if (endIndex > startIndex + 1) endIndex--;
      /* Header? */
      for (int staffIndex = 0; staffIndex < brailleStaves.size(); staffIndex++) {
	for (int i = startIndex; i < endIndex; i++) {
	  int columnWidth = brailleStaves.maxLengthAt(i);
	  if (i == startIndex) {
	    if (staffIndex == 0) {
	      transcriber.printString(paragraphNumber+" ");
            } else {
	      for (int count = 0; count < paragraphNumber.length(); count++) {
		transcriber.printString(" ");
	      }
	      transcriber.printString(" ");
	    }
	    String intro = brailleStaves.get(staffIndex).getIntro();
	    if (intro != null) transcriber.printString(intro);
	  }
	  BrailleMeasure measure = brailleStaves.get(staffIndex).get(i);
	  if (i == startIndex) measure.unlinkPrevious();
	  BrailleList braille = measure.head(1024, false);
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

  class BrailleStaff extends ArrayList<BrailleMeasure> {
    BrailleStaff() {
      super();
    }
    private String intro = null;
    public void setIntro(String intro) { this.intro = intro; }
    public String getIntro() { return intro; }
  }
  class BrailleStaves extends ArrayList<BrailleStaff> {
    BrailleStaves(int initialCapacity) {
      super(initialCapacity);
    }
    public int maxLength(int from, int to) {
      int length = 0;
      for (int i = from; i < to; i++) {
	length += maxLengthAt(i);
      }
      return length + (to - from);
    }
    public int maxLengthAt(int index) {
      int maxLength = 0;
      for (BrailleStaff brailleStaff : this) {
	BrailleMeasure measure = brailleStaff.get(index);
        BrailleList braille = measure.head(1024, false);
	maxLength = Math.max(maxLength, braille.length());
      }
      return maxLength;
    }
    public int getMeasureCount() {
      if (isEmpty()) return 0;
      return get(0).size();
    }
  }
}
