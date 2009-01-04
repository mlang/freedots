package org.delysid.musicxml;

import java.util.ArrayList;
import java.util.List;

import org.delysid.Fraction;
import org.delysid.music.Event;

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
