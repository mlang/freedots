/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.delysid.freedots.model.Event;
import org.delysid.freedots.model.StaffElement;
import org.delysid.freedots.model.MusicList;
import org.delysid.freedots.model.StartBar;
import org.delysid.freedots.model.EndBar;
import org.delysid.freedots.model.VerticalEvent;

import org.delysid.freedots.musicxml.Score;
import org.delysid.freedots.musicxml.Note;
import org.delysid.freedots.musicxml.Part;
import org.delysid.freedots.musicxml.Pitch;

public class Transcriber {
  Score score;

  public Score getScore() { return score; }

  Options options;
  String textStore;
  final String lineSeparator = System.getProperty("line.separator");

  public Transcriber(Score score, Options options) {
    this.score = score;
    this.options = options;
    clear();
    if (score != null)
      try { transcribe(); }
      catch (Exception e) { e.printStackTrace(); }
  }
  private void clear() {
    textStore = "";
  }
  void transcribe() throws Exception {
    for (Part part:score.getParts()) {
      printLine(part.getName());
      for (Segment segment:getSegments(part)) {
        for (int staffIndex=0; staffIndex<segment.getStaffCount(); staffIndex++) {
	  Staff staff = segment.getStaff(staffIndex);
	  MusicList measure = new MusicList();

	  for (int staffElementIndex = 0; staffElementIndex < staff.size();
	       staffElementIndex++) {
	    
	    Event event = staff.get(staffElementIndex);

	    if (event instanceof EndBar) {
	      List<MusicList> voices = measure.getVoices();
	      int voiceCount = voices.size();

	      for (int voiceIndex = 0; voiceIndex < voiceCount; voiceIndex++) {
		BrailleMeasure bm = new BrailleMeasure();
		for (Event voiceEvent:voices.get(voiceIndex)) {
                  bm.add(voiceEvent);
		}
		textStore += bm.toString();
		if (voiceIndex < voiceCount-1) {
		  textStore += "2`";
		}
	      }
	      textStore += " ";

	      measure = new MusicList();
	    } else {
	      measure.add(event);
	    }
          }
        }
      }
    }
  }
  public void printLine(String text) {
    textStore += text + lineSeparator;
  }
  public void printNote(Note note) {
    Pitch pitch = note.getPitch();
    if (pitch != null) {
      String steps[] = { "C", "D", "E", "F", "G", "A", "B"};
      try {
	textStore += steps[pitch.getStep()] + " ";
      } catch (Exception e) {};
    } else {
    }
  }
  public String toString() {
    return textStore;
  }
  class Staff extends MusicList {
    String name;
    public Staff() { super(); }

    public void setName(String name) { this.name = name; }
  }
  class Segment extends MusicList {
    Segment() { super(); }
    public int getStaffCount() {
      for (Event event:this) {
	if (event instanceof StartBar) {
	  StartBar startBar = (StartBar)event;
	  return startBar.getStaffCount();
	}
      }
      return 0;
    }
    public Staff getStaff(int index) {
      List<Staff> staves = new ArrayList<Staff>();
      Map<String, Staff> staffNames = new HashMap<String, Staff>();
      int usedStaves = 0;

      for (int i = 0; i < getStaffCount(); i++)	staves.add(new Staff());
      
      for (Event event:this) {
	if (event instanceof VerticalEvent) {
	  for (Staff staff:staves) staff.add(event);
	} else if (event instanceof StaffElement) {
	  String staffName = ((StaffElement)event).getStaffName();
	  if (!staffNames.containsKey(staffName))
	    staffNames.put(staffName, staves.get(usedStaves++));
	  staffNames.get(staffName).add(event);
	}
      }
      return staves.get(index);
    }
  }
  List<Segment> getSegments(Part part) throws Exception {
    List<Segment> segments = new ArrayList<Segment>();
    Segment currentSegment = new Segment();
    segments.add(currentSegment);
    MusicList musicList = part.getMusicList();
    int index = 0;

    while (true) {
      while (index < musicList.size()) {
	Event event = musicList.get(index++);
	currentSegment.add(event);
	if (event instanceof EndBar) break;
      }

      if (index == musicList.size()) return segments;

      if (!(musicList.get(index) instanceof StartBar))
	throw new Exception();

      StartBar startBar = (StartBar)musicList.get(index);
      if (startBar.getStaffCount() != currentSegment.getStaffCount()) {
	currentSegment = new Segment();
	segments.add(currentSegment);
      }
    }
  }
  class BrailleMeasure {
    List<Object> elements = new ArrayList<Object>();
    public void add(Event staffElement) {
      elements.add(staffElement);
    }
    public String toString() {
      String output = "";
      for (Object element:elements) {
	if (element instanceof Note) {
	  Note note = (Note)element;
	  Pitch pitch = note.getPitch();
	  if (pitch != null) {
	    int noteDots[] = { 145, 15, 124, 1245, 125, 24, 245 };
	    int step = -1;

	    try {
	      step = pitch.getStep();
	    } catch (Exception e) {};

	    if (step != -1) {
	      int unicode = 0X2800 | dotsToBits(noteDots[step]);
	      output += (char)unicode;
	    }
	  } else {
	    output += "r";
	  }
	}
      }
      return output;
    }
  }
  static int dotsToBits(int dots) {
    int bits = 0;
    while (dots > 0) {
      int number = dots % 10;
      dots /= 10;
      bits |= 1 << (number - 1);
    }
    return bits;
  }
}
