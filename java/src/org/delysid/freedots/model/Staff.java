/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.model;

public class Staff extends MusicList {
  String name;
  Timeline<KeySignature> keySignatureList = new Timeline<KeySignature>(
    new KeySignature(0));
  Timeline<Clef> clefList = new Timeline<Clef>(new Clef(Clef.Sign.G, 2));
  public Clef getClef() { return clefList.get(new Fraction(0, 1)); }
  public Clef getClef(Fraction offset) { return clefList.get(offset); }

  public Staff() { super(); }

  public void setName(String name) { this.name = name; }

  public boolean add(Event event) {
    if (super.add(event)) {
      if (event instanceof StaffElement) ((StaffElement)event).setStaff(this);
      if (event instanceof ClefChange) {
        ClefChange clefChange = (ClefChange)event;
        clefList.put(clefChange.getOffset(), clefChange.getClef());
      }
      return true;
    }
    return false;
  }
  public boolean containsChords() {
    for (Event event:this) if (event instanceof StaffChord) return true;
    return false;
  }
}
