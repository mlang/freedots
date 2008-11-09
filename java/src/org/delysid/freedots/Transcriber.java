package org.delysid.freedots;

import java.util.ArrayList;
import java.util.List;
import org.delysid.musicxml.Measure;
import org.delysid.musicxml.MusicXML;
import org.delysid.musicxml.Part;
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
    for (Part part:score.parts()) {
      textStore += part.getName() + "\n";
      for (System system:getSystems(part)) {
        for (int staffIndex=0; staffIndex<system.getStaffCount(); staffIndex++) {
          for (Measure measure:system.measures()) {
            Staff staff = measure.staves(staffIndex);
          }
        }
      }
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
}
