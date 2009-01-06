/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots;

import java.util.ArrayList;
import java.util.List;

import org.delysid.music.Event;

import org.delysid.musicxml.Measure;
import org.delysid.musicxml.MusicXML;
import org.delysid.musicxml.Note;
import org.delysid.musicxml.Part;
import org.delysid.musicxml.Pitch;
import org.delysid.musicxml.Staff;

public class Transcriber {
  MusicXML score;
  Options options;
  String textStore;

  public Transcriber(MusicXML score, Options options) {
    this.score = score;
    this.options = options;
    clear();
    if (score != null) transcribe();
  }
  private void clear() {
    textStore = "";
  }
  void transcribe() {
    for (Part part:score.getParts()) {
      printLine(part.getName());
      for (System system:getSystems(part)) {
        for (int staffIndex=0; staffIndex<system.getStaffCount(); staffIndex++) {
          for (Measure measure:system.measures()) {
            Staff staff = measure.staves(staffIndex);
	    BrailleMeasure brailleMeasure = new BrailleMeasure();

	    for (Event staffElement:staff.getStaffElements()) {
	      brailleMeasure.add(staffElement);
	    }
	    textStore += brailleMeasure.toString() + " ";
          }
        }
      }
    }
  }
  public void printLine(String text) {
    textStore += text + "\n";
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
  class System {
    int staffCount;
    List<Measure> measures = new ArrayList<Measure>();
    public System(Measure firstMeasure) {
      staffCount = firstMeasure.getStaffCount();
      add(firstMeasure);
    }
    public void add(Measure measure) { measures.add(measure); }
    public List<Measure> measures() { return measures; }
    public int getStaffCount() { return staffCount; }
  }
  List<System> getSystems(Part part) {
    List<System> systems = new ArrayList<System>();
    System currentSystem = null;
    for (Measure measure:part.measures())
      if (currentSystem == null || measure.startsNewSystem() ||
          measure.getStaffCount() != currentSystem.getStaffCount())
        systems.add(currentSystem = new System(measure));
      else currentSystem.add(measure);
    return systems;
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
