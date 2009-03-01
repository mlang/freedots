package org.delysid.freedots.musicxml;

public class Slur extends org.delysid.freedots.model.Slur<Note> {
  Slur(Note initialNote) { super(initialNote); }
  @Override
  public boolean add(Note note) {
    if (super.add(note)) {
      note.addSlur(this);
      return true;
    }
    return false;
  }
}
