package org.delysid.freedots.musicxml;

import java.util.ArrayList;
import java.util.List;

import org.delysid.freedots.Fraction;
import org.delysid.freedots.model.Event;

public class Chord extends ArrayList<Note> implements Event {
  Fraction offset;

  Chord() { super(); }
  Chord(Note initialNote) {
    super();
    add(initialNote);
  }
  public Fraction getOffset() { return offset; }
  public void setOffset(Fraction offset) { this.offset = offset; }
}
