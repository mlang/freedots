package org.delysid.freedots.model;

import java.util.ArrayList;
import java.util.List;

public abstract class
AbstractChord<E extends Event> extends ArrayList<E> implements Event {
  Fraction offset;

  AbstractChord(Fraction offset) {
    super();
    this.offset = offset;
  }
  public AbstractChord(E initialNote) {
    this(initialNote.getOffset());
    add(initialNote);
  }
  public Fraction getOffset() { return offset; }
}
